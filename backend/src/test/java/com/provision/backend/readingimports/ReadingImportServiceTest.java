package com.provision.backend.readingimports;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import com.provision.backend.readingimports.api.ReadingImportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingImportServiceTest {
    @Mock ReadingImportRepository importRepository;
    @Mock ReadingImportRowRepository rowRepository;
    @Mock AccountRepository accountRepository;
    @Mock ElectricityMeterRepository meterRepository;
    @Mock MeterReadingRepository readingRepository;
    @InjectMocks ReadingImportService service;

    @Test
    void uploadsAndStagesCsvRows() {
        UUID accountId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "readings.csv", "text/csv", (
                "meter_serial_number,measured_at,zone_t1,zone_t2,zone_t3\n" +
                "SN-1,2026-09-03T06:00:00Z,100.5,50.1,\n").getBytes());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account()));
        when(importRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingImportResponse response = service.upload(file, accountId);

        assertThat(response.status()).isEqualTo(ReadingImportStatus.UPLOADED);
        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.fileHash()).hasSize(64);
        verify(rowRepository).saveAll(any());
    }

    @Test
    void validatesStagedRowsAndMarksImportReady() {
        UUID importId = UUID.randomUUID();
        ReadingImport value = readingImport();
        ReadingImportRow row = row(value);
        ElectricityMeter meter = mock(ElectricityMeter.class);
        UUID meterId = UUID.randomUUID();
        when(meter.getId()).thenReturn(meterId);
        when(importRepository.findById(importId)).thenReturn(Optional.of(value));
        when(rowRepository.findAllByReadingImportIdOrderByRowNumber(importId)).thenReturn(List.of(row));
        when(meterRepository.findBySerialNumber("SN-1")).thenReturn(Optional.of(meter));

        ReadingImportResponse response = service.validate(importId);

        assertThat(response.status()).isEqualTo(ReadingImportStatus.READY);
        assertThat(response.validRows()).isEqualTo(1);
        assertThat(row.getElectricityMeter()).isSameAs(meter);
    }

    @Test
    void findsImportWithRows() {
        UUID importId = UUID.randomUUID();
        ReadingImport value = readingImport();
        ReadingImportRow row = row(value);
        when(importRepository.findById(importId)).thenReturn(Optional.of(value));
        when(rowRepository.findAllByReadingImportIdOrderByRowNumber(importId)).thenReturn(List.of(row));

        assertThat(service.findById(importId).rows()).hasSize(1);
    }

    @Test
    void locksMetersAndBatchAppliesReadyImport() {
        UUID importId = UUID.randomUUID();
        ReadingImport value = readingImport();
        value.setStatus(ReadingImportStatus.READY);
        ReadingImportRow row = row(value);
        ElectricityMeter meter = mock(ElectricityMeter.class);
        UUID meterId = UUID.randomUUID();
        when(meter.getId()).thenReturn(meterId);
        row.setElectricityMeter(meter);
        when(importRepository.findByIdForUpdate(importId)).thenReturn(Optional.of(value));
        when(rowRepository.findAllByReadingImportIdOrderByRowNumber(importId)).thenReturn(List.of(row));

        ReadingImportResponse response = service.apply(importId);

        assertThat(response.status()).isEqualTo(ReadingImportStatus.APPLIED);
        verify(meterRepository).findAllByIdForUpdate(List.of(meterId));
        verify(readingRepository).saveAllAndFlush(argThat(values -> {
            MeterReading reading = values.iterator().next();
            return reading.getSourceType().name().equals("CSV");
        }));
    }

    private ReadingImport readingImport() {
        return new ReadingImport("readings.csv", "hash", account());
    }

    private ReadingImportRow row(ReadingImport value) {
        return new ReadingImportRow(value, 2, "SN-1", Instant.parse("2026-09-03T06:00:00Z"),
                new BigDecimal("100.5"), new BigDecimal("50.1"), null);
    }

    private Account account() {
        return new Account("admin@example.com", "Администратор", "hash", AccountRole.ADMIN);
    }
}
