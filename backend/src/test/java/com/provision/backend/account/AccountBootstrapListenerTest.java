package com.provision.backend.account;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountBootstrapListenerTest {
    @Test
    void createsOnlyAccountsForMissingRequiredRoles() {
        AccountRepository repository = mock(AccountRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.existsByRole(AccountRole.ADMIN)).thenReturn(false);
        when(repository.existsByRole(AccountRole.MANAGER)).thenReturn(true);
        when(encoder.encode(anyString())).thenReturn("encoded-password");
        AccountBootstrapListener listener = new AccountBootstrapListener(repository, encoder);

        listener.createRequiredAccounts();

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(accountCaptor.capture());
        Account created = accountCaptor.getValue();
        assertThat(created.getRole()).isEqualTo(AccountRole.ADMIN);
        assertThat(created.getEmail()).isEqualTo("admin@provision.local");
        assertThat(created.getPasswordHash()).isEqualTo("encoded-password");
        verify(repository).existsByRole(AccountRole.ADMIN);
        verify(repository).existsByRole(AccountRole.MANAGER);
    }
}
