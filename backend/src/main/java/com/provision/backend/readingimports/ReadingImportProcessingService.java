package com.provision.backend.readingimports;

import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import com.provision.backend.meterreadings.MeterReadingSourceType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReadingImportProcessingService {
    private static final int BATCH_SIZE = 100;

    private final ReadingImportRepository importRepository;
    private final ReadingImportRowRepository rowRepository;
    private final ElectricityMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final EntityManager entityManager;

    @Transactional
    public boolean startValidation(UUID importId) {
        ReadingImport readingImport = getForUpdate(importId);
        if (readingImport.getStatus() != ReadingImportStatus.UPLOADED) {
            return false;
        }
        readingImport.setStatus(ReadingImportStatus.VALIDATING);
        return true;
    }

    @Transactional
    public boolean validate(UUID importId) {
        ReadingImport readingImport = getForUpdate(importId);
        if (readingImport.getStatus() != ReadingImportStatus.VALIDATING) {
            return false;
        }
        Set<String> keys = new HashSet<>();
        List<ReadingImportRow> batch = new ArrayList<>(BATCH_SIZE);
        int valid = 0;
        try (Stream<ReadingImportRow> rows = rowRepository.streamAllByImportId(importId)) {
            Iterator<ReadingImportRow> iterator = rows.iterator();
            while (iterator.hasNext()) {
                ReadingImportRow row = iterator.next();
                if (validate(row, keys)) {
                    valid++;
                }
                batch.add(row);
                if (batch.size() == BATCH_SIZE) {
                    saveRowBatch(batch);
                }
            }
        }
        saveRowBatch(batch);

        readingImport = getForUpdate(importId);
        readingImport.setValidRows(valid);
        readingImport.setInvalidRows(readingImport.getTotalRows() - valid);
        readingImport.setValidatedAt(Instant.now());
        readingImport.setStatus(valid == readingImport.getTotalRows() && valid > 0
                ? ReadingImportStatus.READY : ReadingImportStatus.INVALID);
        return readingImport.getStatus() == ReadingImportStatus.READY;
    }

    @Transactional
    public boolean startApplying(UUID importId) {
        ReadingImport readingImport = getForUpdate(importId);
        if (readingImport.getStatus() != ReadingImportStatus.READY) {
            return false;
        }
        readingImport.setStatus(ReadingImportStatus.APPLYING);
        return true;
    }

    @Transactional
    public void apply(UUID importId) {
        ReadingImport readingImport = getForUpdate(importId);
        if (readingImport.getStatus() != ReadingImportStatus.APPLYING) {
            return;
        }
        List<MeterReading> batch = new ArrayList<>(BATCH_SIZE);
        try (Stream<ReadingImportRow> rows = rowRepository.streamAllByImportId(importId)) {
            rows.forEach(row -> {
                MeterReading reading = new MeterReading(row.getElectricityMeter(), row.getMeasuredAt(),
                        row.getZoneT1(), MeterReadingSourceType.CSV);
                reading.setZoneT2(row.getZoneT2());
                reading.setZoneT3(row.getZoneT3());
                batch.add(reading);
                if (batch.size() == BATCH_SIZE) {
                    saveReadingBatch(batch);
                }
            });
        }
        saveReadingBatch(batch);

        readingImport = getForUpdate(importId);
        readingImport.setStatus(ReadingImportStatus.APPLIED);
        readingImport.setAppliedAt(Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID importId) {
        importRepository.findByIdForUpdate(importId).ifPresent(readingImport ->
                readingImport.setStatus(ReadingImportStatus.FAILED));
    }

    private boolean validate(ReadingImportRow row, Set<String> keys) {
        String error = row.getValidationError();
        Optional<ElectricityMeter> meter = meterRepository.findBySerialNumber(row.getMeterSerialNumber());
        if (meter.isEmpty()) {
            error = append(error, "Прибор с таким серийным номером не найден");
        } else {
            row.setElectricityMeter(meter.get());
            String key = meter.get().getId() + "|" + row.getMeasuredAt();
            if (!keys.add(key)) {
                error = append(error, "Дубликат прибора и времени внутри файла");
            }
        }
        row.setValidationError(error);
        return error == null;
    }

    private void saveRowBatch(List<ReadingImportRow> batch) {
        if (batch.isEmpty()) return;
        rowRepository.saveAllAndFlush(batch);
        entityManager.clear();
        batch.clear();
    }

    private void saveReadingBatch(List<MeterReading> batch) {
        if (batch.isEmpty()) return;
        readingRepository.saveAllAndFlush(batch);
        entityManager.clear();
        batch.clear();
    }

    private ReadingImport getForUpdate(UUID id) {
        return importRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReadingImportException("Импорт не найден"));
    }

    private String append(String current, String next) {
        return current == null ? next : current + "; " + next;
    }
}
