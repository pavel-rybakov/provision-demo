package com.provision.backend.reports;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;

@Component
@RequiredArgsConstructor
public class LatestReadingsCsvGenerator {
    private static final String SQL = """
            SELECT m.id,
                   m.serial_number,
                   m.inventory_number,
                   r.measured_at,
                   r.zone_t1,
                   r.zone_t2,
                   r.zone_t3
            FROM electricity_meter m
            LEFT JOIN LATERAL (
                SELECT mr.measured_at, mr.zone_t1, mr.zone_t2, mr.zone_t3
                FROM meter_reading mr
                WHERE mr.electricity_meter_id = m.id
                ORDER BY mr.measured_at DESC
                LIMIT 1
            ) r ON TRUE
            ORDER BY m.id
            """;

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("meter_id", "meter_serial_number", "inventory_number",
                    "measured_at", "zone_t1", "zone_t2", "zone_t3")
            .get();

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Path generate() {
        Path file = createTempFile();
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(writer, FORMAT)) {
            jdbcTemplate.query(connection -> {
                PreparedStatement statement = connection.prepareStatement(SQL);
                statement.setFetchSize(1000);
                return statement;
            }, resultSet -> {
                try {
                    csv.printRecord(
                            resultSet.getObject("id"),
                            resultSet.getString("serial_number"),
                            resultSet.getString("inventory_number"),
                            resultSet.getObject("measured_at"),
                            resultSet.getBigDecimal("zone_t1"),
                            resultSet.getBigDecimal("zone_t2"),
                            resultSet.getBigDecimal("zone_t3")
                    );
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
            return file;
        } catch (RuntimeException | IOException exception) {
            deleteQuietly(file);
            throw new LatestReadingsReportException("Не удалось сформировать CSV-отчёт", exception);
        }
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("latest-meter-readings-", ".csv");
        } catch (IOException exception) {
            throw new LatestReadingsReportException("Не удалось создать временный CSV-файл", exception);
        }
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // The original generation error is more important; OS cleanup handles abandoned temp files.
        }
    }
}
