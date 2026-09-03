package com.provision.backend.meter;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meter.api.ElectricityMeterRequest;
import com.provision.backend.meter.api.ElectricityMeterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectricityMeterServiceTest {
    @Mock ElectricityMeterRepository meterRepository;
    @Mock AccountRepository accountRepository;
    @InjectMocks ElectricityMeterService meterService;

    @Test
    void createsMeter() {
        Account account = account();
        UUID currentAccountId = UUID.randomUUID();
        ElectricityMeterRequest request = request("SN-1");
        when(accountRepository.findById(currentAccountId)).thenReturn(Optional.of(account));
        when(meterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ElectricityMeterResponse response = meterService.create(request, currentAccountId);

        assertThat(response.serialNumber()).isEqualTo("SN-1");
        assertThat(response.transformationRatio()).isEqualByComparingTo("100.5");
        verify(meterRepository).save(any(ElectricityMeter.class));
    }

    @Test
    void findsAllMeters() {
        ElectricityMeter meter = new ElectricityMeter(account(), "SN-1", "INV-1");
        when(meterRepository.findAll(any(Sort.class))).thenReturn(List.of(meter));

        assertThat(meterService.findAll()).extracting(ElectricityMeterResponse::serialNumber)
                .containsExactly("SN-1");
    }

    @Test
    void findsMeterById() {
        UUID id = UUID.randomUUID();
        ElectricityMeter meter = new ElectricityMeter(account(), "SN-1", "INV-1");
        when(meterRepository.findById(id)).thenReturn(Optional.of(meter));

        assertThat(meterService.findById(id).inventoryNumber()).isEqualTo("INV-1");
    }

    @Test
    void updatesMeter() {
        UUID id = UUID.randomUUID();
        UUID currentAccountId = UUID.randomUUID();
        ElectricityMeter meter = new ElectricityMeter(account(), "OLD", "OLD-INV");
        when(meterRepository.findById(id)).thenReturn(Optional.of(meter));
        Account updatingAccount = account();
        when(accountRepository.findById(currentAccountId)).thenReturn(Optional.of(updatingAccount));

        ElectricityMeterResponse response = meterService.update(id, request("NEW"), currentAccountId);

        assertThat(response.serialNumber()).isEqualTo("NEW");
        assertThat(meter.getInventoryNumber()).isEqualTo("INV-1");
        assertThat(meter.getUpdatedBy()).isSameAs(updatingAccount);
        verify(meterRepository, never()).save(any());
    }

    @Test
    void deletesMeter() {
        UUID id = UUID.randomUUID();
        ElectricityMeter meter = new ElectricityMeter(account(), "SN-1", "INV-1");
        when(meterRepository.findById(id)).thenReturn(Optional.of(meter));

        meterService.delete(id);

        verify(meterRepository).delete(meter);
    }

    private Account account() {
        return new Account("manager@example.com", "Менеджер", "hash", AccountRole.MANAGER);
    }

    private ElectricityMeterRequest request(String serialNumber) {
        return new ElectricityMeterRequest(
                serialNumber, "INV-1", 2025, new BigDecimal("100.5"),
                LocalDate.of(2025, 1, 1), "SEAL", "ANTI", "Щитовая", "Примечание", "GIS-1"
        );
    }
}
