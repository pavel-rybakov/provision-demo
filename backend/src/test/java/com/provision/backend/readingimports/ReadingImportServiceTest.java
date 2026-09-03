package com.provision.backend.readingimports;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import com.provision.backend.readingimports.api.ReadingImportResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingImportServiceTest {
    @Mock ReadingImportRepository importRepository;
    @Mock ReadingImportRowRepository rowRepository;
    @Mock AccountRepository accountRepository;
    @Mock ElectricityMeterRepository meterRepository;
    @Mock MeterReadingRepository readingRepository;
    @Mock EntityManager entityManager;
    @InjectMocks ReadingImportService service;

    @Test
    void uploadsValidatesAndAppliesCsvInOneCall() {
        UUID accountId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "readings.csv", "text/csv", (
                "meter_serial_number,measured_at,zone_t1,zone_t2,zone_t3\n" +
                "SN-1,2026-09-03T06:00:00Z,100.5,50.1,\n").getBytes());
        ElectricityMeter meter = mock(ElectricityMeter.class);
        UUID meterId = UUID.randomUUID();
        when(meter.getId()).thenReturn(meterId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account()));
        when(importRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(meterRepository.findBySerialNumber("SN-1")).thenReturn(Optional.of(meter));
        when(rowRepository.streamAllByImportId(org.mockito.ArgumentMatchers.nullable(UUID.class))).thenAnswer(invocation ->
                java.util.stream.Stream.of(row(readingImport(), meter)));

        ReadingImportResponse response = service.importReadings(file, accountId);

        assertThat(response.status()).isEqualTo(ReadingImportStatus.APPLIED);
        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.validRows()).isEqualTo(1);
        assertThat(response.fileHash()).hasSize(64);
        verify(rowRepository).saveAllAndFlush(any());
        verify(readingRepository).saveAllAndFlush(any());
    }

    @Test
    void validatesAllRowsButDoesNotApplyAnyWhenOneRowIsInvalid() {
        UUID accountId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "readings.csv", "text/csv", (
                "meter_serial_number,measured_at,zone_t1,zone_t2,zone_t3\n" +
                "SN-1,2026-09-03T06:00:00Z,100.5,,\n" +
                "UNKNOWN,2026-09-03T07:00:00Z,110.5,,\n").getBytes());
        ElectricityMeter meter = mock(ElectricityMeter.class);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account()));
        when(importRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(meterRepository.findBySerialNumber("SN-1")).thenReturn(Optional.of(meter));

        ReadingImportResponse response = service.importReadings(file, accountId);

        assertThat(response.status()).isEqualTo(ReadingImportStatus.INVALID);
        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.validRows()).isEqualTo(1);
        assertThat(response.invalidRows()).isEqualTo(1);
        verify(readingRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void findsImport() {
        UUID importId = UUID.randomUUID();
        ReadingImport value = readingImport();
        when(importRepository.findById(importId)).thenReturn(Optional.of(value));

        assertThat(service.findById(importId).originalFilename()).isEqualTo("readings.csv");
    }

    @Test
    void savesRowsInBatchesOfOneHundred() {
        UUID accountId = UUID.randomUUID();
        StringBuilder csv = new StringBuilder("meter_serial_number,measured_at,zone_t1,zone_t2,zone_t3\n");
        for (int i = 0; i < 101; i++) {
            csv.append("UNKNOWN,2026-09-03T06:00:00Z,100,,\n");
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "readings.csv", "text/csv", csv.toString().getBytes());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account()));
        when(importRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingImportResponse response = service.importReadings(file, accountId);

        assertThat(response.totalRows()).isEqualTo(101);
        verify(rowRepository, times(2)).saveAllAndFlush(any());
        verify(entityManager, times(2)).clear();
    }

    private ReadingImport readingImport() {
        return new ReadingImport("readings.csv", "hash", account());
    }

    private ReadingImportRow row(ReadingImport value) {
        return new ReadingImportRow(value, 2, "SN-1", Instant.parse("2026-09-03T06:00:00Z"),
                new BigDecimal("100.5"), new BigDecimal("50.1"), null);
    }

    private ReadingImportRow row(ReadingImport value, ElectricityMeter meter) {
        ReadingImportRow row = row(value);
        row.setElectricityMeter(meter);
        return row;
    }

    private Account account() {
        return new Account("admin@example.com", "Администратор", "hash", AccountRole.ADMIN);
    }
}
