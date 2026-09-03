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
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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

@Service
@RequiredArgsConstructor
public class ReadingImportService {
    private static final List<String> HEADERS = List.of(
            "meter_serial_number", "measured_at", "zone_t1", "zone_t2", "zone_t3");

    private final ReadingImportRepository importRepository;
    private final ReadingImportRowRepository rowRepository;
    private final AccountRepository accountRepository;
    private final ElectricityMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;

    @Transactional
    public ReadingImportResponse upload(MultipartFile file, UUID currentAccountId) {
        byte[] content = read(file);
        String hash = sha256(content);
        if (importRepository.existsByFileHash(hash)) {
            throw new ReadingImportException("Этот CSV-файл уже был загружен");
        }
        Account uploader = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new AccountNotFoundException(currentAccountId));

        // TODO: загрузить оригинальный файл в S3-compatible object storage и сохранить object key.
        ReadingImport readingImport = importRepository.save(
                new ReadingImport(Optional.ofNullable(file.getOriginalFilename()).orElse("readings.csv"), hash, uploader)
        );

        List<ReadingImportRow> rows = parse(content, readingImport);
        readingImport.setTotalRows(rows.size());
        rowRepository.saveAll(rows);
        return ReadingImportResponse.from(readingImport, rows);
    }

    @Transactional
    public ReadingImportResponse validate(UUID id) {
        ReadingImport readingImport = get(id);
        if (readingImport.getStatus() == ReadingImportStatus.APPLIED) {
            throw new ReadingImportException("Применённый импорт нельзя проверить повторно");
        }
        List<ReadingImportRow> rows = rows(id);
        Set<String> keys = new HashSet<>();
        int valid = 0;
        for (ReadingImportRow row : rows) {
            String error = row.getValidationError();
            Optional<ElectricityMeter> meter = meterRepository.findBySerialNumber(row.getMeterSerialNumber());
            if (meter.isEmpty()) error = append(error, "Прибор с таким серийным номером не найден");
            else {
                row.setElectricityMeter(meter.get());
                String key = meter.get().getId() + "|" + row.getMeasuredAt();
                if (!keys.add(key)) error = append(error, "Дубликат прибора и времени внутри файла");
                if (row.getMeasuredAt() != null && readingRepository
                        .existsByElectricityMeterIdAndMeasuredAt(meter.get().getId(), row.getMeasuredAt())) {
                    error = append(error, "Показание с таким временем уже существует");
                }
            }
            row.setValidationError(error);
            if (error == null) valid++;
        }
        readingImport.setValidRows(valid);
        readingImport.setInvalidRows(rows.size() - valid);
        readingImport.setValidatedAt(Instant.now());
        readingImport.setStatus(valid == rows.size() && !rows.isEmpty()
                ? ReadingImportStatus.READY : ReadingImportStatus.INVALID);
        return ReadingImportResponse.from(readingImport, rows);
    }

    @Transactional(readOnly = true)
    public ReadingImportResponse findById(UUID id) {
        return ReadingImportResponse.from(get(id), rows(id));
    }

    @Transactional
    public ReadingImportResponse apply(UUID id) {
        ReadingImport readingImport = importRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReadingImportException("Импорт не найден"));
        if (readingImport.getStatus() != ReadingImportStatus.READY) {
            throw new ReadingImportException("Применить можно только импорт в статусе READY");
        }
        List<ReadingImportRow> rows = rows(id);
        List<UUID> meterIds = rows.stream().map(row -> row.getElectricityMeter().getId()).distinct().sorted().toList();
        meterRepository.findAllByIdForUpdate(meterIds);

        for (ReadingImportRow row : rows) {
            if (readingRepository.existsByElectricityMeterIdAndMeasuredAt(
                    row.getElectricityMeter().getId(), row.getMeasuredAt())) {
                throw new ReadingImportException("После проверки появились конфликтующие показания; проверьте импорт снова");
            }
        }
        List<MeterReading> readings = rows.stream().map(row -> {
            MeterReading reading = new MeterReading(row.getElectricityMeter(), row.getMeasuredAt(),
                    row.getZoneT1(), MeterReadingSourceType.CSV);
            reading.setZoneT2(row.getZoneT2());
            reading.setZoneT3(row.getZoneT3());
            return reading;
        }).toList();
        readingRepository.saveAllAndFlush(readings);
        readingImport.setStatus(ReadingImportStatus.APPLIED);
        readingImport.setAppliedAt(Instant.now());
        return ReadingImportResponse.from(readingImport, rows);
    }

    private List<ReadingImportRow> parse(byte[] content, ReadingImport readingImport) {
        try (InputStreamReader reader = new InputStreamReader(
                new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();
            try (CSVParser parser = format.parse(reader)) {
                if (!parser.getHeaderNames().equals(HEADERS)) {
                    throw new ReadingImportException("Ожидаются колонки: " + String.join(",", HEADERS));
                }
                List<ReadingImportRow> result = new ArrayList<>();
                for (CSVRecord record : parser) {
                    result.add(parseRow(readingImport, record));
                }
                return result;
            }
        } catch (ReadingImportException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ReadingImportException("Не удалось прочитать CSV-файл", exception);
        }
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
            if (required) throw new NumberFormatException();
            return null;
        }
        return new BigDecimal(trimmed);
    }

    private ReadingImport get(UUID id) {
        return importRepository.findById(id).orElseThrow(() -> new ReadingImportException("Импорт не найден"));
    }

    private List<ReadingImportRow> rows(UUID id) { return rowRepository.findAllByReadingImportIdOrderByRowNumber(id); }

    private String append(String current, String next) { return current == null ? next : current + "; " + next; }

    private byte[] read(MultipartFile file) {
        try { return file.getBytes(); }
        catch (Exception e) { throw new ReadingImportException("Не удалось прочитать загруженный файл", e); }
    }

    private String sha256(byte[] content) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
