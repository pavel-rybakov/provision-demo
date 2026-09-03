package com.provision.backend.readingimports;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountNotFoundException;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import com.provision.backend.meterreadings.MeterReadingSourceType;
import com.provision.backend.readingimports.api.ReadingImportResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReadingImportService {
    private static final int BATCH_SIZE = 100;
    private static final List<String> HEADERS = List.of(
            "meter_serial_number", "measured_at", "zone_t1", "zone_t2", "zone_t3");

    private final ReadingImportRepository importRepository;
    private final ReadingImportRowRepository rowRepository;
    private final AccountRepository accountRepository;
    private final ElectricityMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final EntityManager entityManager;

    @Transactional
    public ReadingImportResponse importReadings(MultipartFile file, UUID currentAccountId) {
        String hash = sha256(file);
        if (importRepository.existsByFileHash(hash)) {
            throw new ReadingImportException("Этот CSV-файл уже был загружен");
        }
        Account uploader = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new AccountNotFoundException(currentAccountId));

        // TODO: загрузить оригинальный файл в S3-compatible object storage и сохранить object key.
        ReadingImport readingImport = importRepository.save(
                new ReadingImport(Optional.ofNullable(file.getOriginalFilename()).orElse("readings.csv"), hash, uploader)
        );

        parseValidateAndSave(file, readingImport);
        if (readingImport.getStatus() == ReadingImportStatus.READY) {
            apply(readingImport);
        }
        importRepository.save(readingImport);
        return ReadingImportResponse.from(readingImport);
    }

    private void parseValidateAndSave(MultipartFile file, ReadingImport readingImport) {
        Set<String> keys = new HashSet<>();
        List<ReadingImportRow> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;
        int valid = 0;
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();
            try (CSVParser parser = format.parse(reader)) {
                if (!parser.getHeaderNames().equals(HEADERS)) {
                    throw new ReadingImportException("Ожидаются колонки: " + String.join(",", HEADERS));
                }
                for (CSVRecord record : parser) {
                    ReadingImportRow row = parseRow(readingImport, record);
                    if (validate(row, keys)) {
                        valid++;
                    }
                    total++;
                    batch.add(row);
                    if (batch.size() == BATCH_SIZE) {
                        saveRowBatch(batch);
                    }
                }
                saveRowBatch(batch);
            }
        } catch (ReadingImportException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ReadingImportException("Не удалось прочитать CSV-файл", exception);
        }
        readingImport.setTotalRows(total);
        readingImport.setValidRows(valid);
        readingImport.setInvalidRows(total - valid);
        readingImport.setValidatedAt(Instant.now());
        readingImport.setStatus(valid == total && total > 0
                ? ReadingImportStatus.READY : ReadingImportStatus.INVALID);
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
        if (batch.isEmpty()) {
            return;
        }
        rowRepository.saveAllAndFlush(batch);
        entityManager.clear();
        batch.clear();
    }

    @Transactional(readOnly = true)
    public ReadingImportResponse findById(UUID id) {
        return ReadingImportResponse.from(get(id));
    }

    private void apply(ReadingImport readingImport) {
        List<MeterReading> batch = new ArrayList<>(BATCH_SIZE);
        try (Stream<ReadingImportRow> rows = rowRepository.streamAllByImportId(readingImport.getId())) {
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
        readingImport.setStatus(ReadingImportStatus.APPLIED);
        readingImport.setAppliedAt(Instant.now());
    }

    private void saveReadingBatch(List<MeterReading> batch) {
        if (batch.isEmpty()) {
            return;
        }
        readingRepository.saveAllAndFlush(batch);
        entityManager.clear();
        batch.clear();
    }

    private ReadingImportRow parseRow(ReadingImport readingImport, CSVRecord record) {
        String error = null;
        Instant measuredAt = null;
        BigDecimal t1 = null;
        try {
            measuredAt = Instant.parse(record.get("measured_at"));
        } catch (DateTimeParseException e) {
            error = append(error, "Некорректный measured_at");
        }

        try {
            t1 = decimal(record.get("zone_t1"), true);
        } catch (NumberFormatException e) {
            error = append(error, "Некорректный zone_t1");
        }

        BigDecimal t2 = null;
        try {
            t2 = decimal(record.get("zone_t2"), false);
        } catch (NumberFormatException e) {
            error = append(error, "Некорректный zone_t2");
        }

        BigDecimal t3 = null;
        try {
            t3 = decimal(record.get("zone_t3"), false);
        } catch (NumberFormatException e) {
            error = append(error, "Некорректный zone_t3");
        }

        if (t1 != null && t1.signum() < 0 || t2 != null && t2.signum() < 0 || t3 != null && t3.signum() < 0) {
            error = append(error, "Значения зон не могут быть отрицательными");
        }

        if (t3 != null && t2 == null) {
            error = append(error, "zone_t3 нельзя передать без zone_t2");
        }

        String serial = record.get("meter_serial_number").trim();
        if (serial.isEmpty()) {
            error = append(error, "Не указан meter_serial_number");
        }

        ReadingImportRow row = new ReadingImportRow(readingImport, (int) record.getRecordNumber() + 1, serial, measuredAt, t1, t2, t3);
        row.setValidationError(error);
        return row;
    }

    private BigDecimal decimal(String value, boolean required) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            if (required) {
                throw new NumberFormatException();
            }
            return null;
        }
        return new BigDecimal(trimmed);
    }

    private ReadingImport get(UUID id) {
        return importRepository.findById(id).orElseThrow(() -> new ReadingImportException("Импорт не найден"));
    }

    private String append(String current, String next) { return current == null ? next : current + "; " + next; }

    private String sha256(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) != -1;) digest.update(buffer, 0, read);
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new ReadingImportException("Не удалось прочитать загруженный файл", e);
        }
    }
}
