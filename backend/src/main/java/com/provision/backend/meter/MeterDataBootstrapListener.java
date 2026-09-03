package com.provision.backend.meter;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(20)
@ConditionalOnProperty(
        prefix = "app.bootstrap-meter-data",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MeterDataBootstrapListener {
    private static final int METER_COUNT = 10;
    private static final int READINGS_PER_METER = 5;

    private final ElectricityMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final AccountRepository accountRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createSampleMeterData() {
        if (meterRepository.count() > 0) {
            return;
        }

        Account admin = accountRepository.findFirstByRole(AccountRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Невозможно создать тестовые приборы: отсутствует учётная запись ADMIN"));
        List<ElectricityMeter> meters = createMeters(admin);
        meterRepository.saveAllAndFlush(meters);
        readingRepository.saveAll(createReadings(meters));
        log.warn("Созданы тестовые данные: {} приборов и {} показаний",
                meters.size(), meters.size() * READINGS_PER_METER);
    }

    private List<ElectricityMeter> createMeters(Account admin) {
        List<ElectricityMeter> meters = new ArrayList<>(METER_COUNT);
        for (int index = 1; index <= METER_COUNT; index++) {
            ElectricityMeter meter = new ElectricityMeter(
                    admin,
                    "TEST-SN-%04d".formatted(index),
                    "TEST-INV-%04d".formatted(index)
            );
            meter.setManufactureYear(2015 + index);
            meter.setTransformationRatio(BigDecimal.valueOf(index % 3 == 0 ? 100 : 1));
            meter.setInstallationDate(LocalDate.now().minusMonths(index));
            meter.setSealNumber("TEST-SEAL-%04d".formatted(index));
            meter.setAntimagneticSealNumber("TEST-ANTI-%04d".formatted(index));
            meter.setInstallationLocation("Тестовый объект, помещение %d".formatted(index));
            meter.setNote("Автоматически созданные тестовые данные");
            meter.setGisHousingId("TEST-GIS-%04d".formatted(index));
            meters.add(meter);
        }
        return meters;
    }

    private List<MeterReading> createReadings(List<ElectricityMeter> meters) {
        List<MeterReading> readings = new ArrayList<>(meters.size() * READINGS_PER_METER);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        for (int meterIndex = 0; meterIndex < meters.size(); meterIndex++) {
            ElectricityMeter meter = meters.get(meterIndex);
            for (int readingIndex = 0; readingIndex < READINGS_PER_METER; readingIndex++) {
                int daysAgo = READINGS_PER_METER - readingIndex;
                BigDecimal base = BigDecimal.valueOf((meterIndex + 1L) * 1_000L + readingIndex * 25L);
                MeterReading reading = new MeterReading(meter, now.minus(daysAgo, ChronoUnit.DAYS), base);
                reading.setZoneT2(base.multiply(new BigDecimal("0.55")));
                readings.add(reading);
            }
        }
        return readings;
    }
}
