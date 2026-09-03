package com.provision.backend.meter;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeterDataBootstrapListenerTest {
    @Test
    @SuppressWarnings("unchecked")
    void createsTenMetersAndFiveReadingsForEachWhenMetersAreAbsent() {
        ElectricityMeterRepository meterRepository = mock(ElectricityMeterRepository.class);
        MeterReadingRepository readingRepository = mock(MeterReadingRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        Account admin = new Account("admin@example.com", "Администратор", "hash", AccountRole.ADMIN);
        when(meterRepository.count()).thenReturn(0L);
        when(accountRepository.findFirstByRole(AccountRole.ADMIN)).thenReturn(Optional.of(admin));
        MeterDataBootstrapListener listener = new MeterDataBootstrapListener(
                meterRepository, readingRepository, accountRepository);

        listener.createSampleMeterData();

        ArgumentCaptor<List<ElectricityMeter>> metersCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<MeterReading>> readingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(meterRepository).saveAllAndFlush(metersCaptor.capture());
        verify(readingRepository).saveAll(readingsCaptor.capture());
        assertThat(metersCaptor.getValue()).hasSize(10);
        assertThat(readingsCaptor.getValue()).hasSize(50);
        assertThat(readingsCaptor.getValue()).allMatch(reading -> reading.getZoneT1().signum() > 0);
    }
}
