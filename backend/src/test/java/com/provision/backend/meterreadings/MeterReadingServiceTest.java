package com.provision.backend.meterreadings;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.api.CreateMeterReadingRequest;
import com.provision.backend.meterreadings.api.MeterReadingResponse;
import com.provision.backend.meterreadings.api.UpdateMeterReadingRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceTest {
    private static final Instant MEASURED_AT = Instant.parse("2026-09-03T06:00:00Z");

    @Mock MeterReadingRepository readingRepository;
    @Mock ElectricityMeterRepository meterRepository;
    @InjectMocks MeterReadingService readingService;

    @Test
    void createsReading() {
        UUID meterId = UUID.randomUUID();
        ElectricityMeter meter = meter();
        CreateMeterReadingRequest request = new CreateMeterReadingRequest(
                meterId, MEASURED_AT, decimal("100"), decimal("50"), null
        );
        when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
        when(readingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MeterReadingResponse response = readingService.create(request);

        assertThat(response.zoneT1()).isEqualByComparingTo("100");
        assertThat(response.zoneT2()).isEqualByComparingTo("50");
        verify(readingRepository).save(any(MeterReading.class));
    }

    @Test
    void findsAllReadings() {
        MeterReading reading = new MeterReading(meter(), MEASURED_AT, decimal("100"));
        when(readingRepository.findAll(any(Sort.class))).thenReturn(List.of(reading));

        assertThat(readingService.findAll()).extracting(MeterReadingResponse::measuredAt)
                .containsExactly(MEASURED_AT);
    }

    @Test
    void findsReadingById() {
        UUID id = UUID.randomUUID();
        MeterReading reading = new MeterReading(meter(), MEASURED_AT, decimal("100"));
        when(readingRepository.findById(id)).thenReturn(Optional.of(reading));

        assertThat(readingService.findById(id).zoneT1()).isEqualByComparingTo("100");
    }

    @Test
    void updatesReading() {
        UUID id = UUID.randomUUID();
        MeterReading reading = new MeterReading(meter(), MEASURED_AT, decimal("100"));
        when(readingRepository.findById(id)).thenReturn(Optional.of(reading));
        Instant updatedTime = MEASURED_AT.plusSeconds(3600);

        MeterReadingResponse response = readingService.update(
                id, new UpdateMeterReadingRequest(updatedTime, decimal("110"), decimal("55"), decimal("20"))
        );

        assertThat(response.measuredAt()).isEqualTo(updatedTime);
        assertThat(reading.getZoneT3()).isEqualByComparingTo("20");
        verify(readingRepository, never()).save(any());
    }

    @Test
    void deletesReading() {
        UUID id = UUID.randomUUID();
        MeterReading reading = new MeterReading(meter(), MEASURED_AT, decimal("100"));
        when(readingRepository.findById(id)).thenReturn(Optional.of(reading));

        readingService.delete(id);

        verify(readingRepository).delete(reading);
    }

    private ElectricityMeter meter() {
        Account account = new Account("manager@example.com", "Менеджер", "hash", AccountRole.MANAGER);
        return new ElectricityMeter(account, "SN-1", "INV-1");
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
