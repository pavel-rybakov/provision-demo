package com.provision.backend.readingimports;

import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DebugReadingImportCsvGenerator {
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("meter_serial_number", "measured_at", "zone_t1", "zone_t2", "zone_t3")
            .get();

    private final ElectricityMeterRepository meterRepository;

    public StreamingResponseBody generate(int rowCount) {
        if (rowCount < 1 || rowCount > 1_000_000) {
            throw new ReadingImportException("Количество строк должно быть от 1 до 1000000");
        }

        ElectricityMeter meter = meterRepository
                .findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ReadingImportException("Для генерации CSV нужен хотя бы один прибор"));
        String serialNumber = meter.getSerialNumber();
        Instant firstMeasuredAt = Instant.now();

        return outputStream -> {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            try (CSVPrinter csv = new CSVPrinter(writer, FORMAT)) {
                for (int index = 0; index < rowCount; index++) {
                    csv.printRecord(
                            serialNumber,
                            firstMeasuredAt.plusMillis(index),
                            index + 1,
                            "",
                            ""
                    );
                }
            }
        };
    }
}
