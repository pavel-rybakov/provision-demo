package com.provision.backend.account;

import com.provision.backend.account.api.AccountResponse;
import com.provision.backend.account.api.CreateAccountRequest;
import com.provision.backend.account.api.UpdateAccountRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createsAccount() {
        CreateAccountRequest request = new CreateAccountRequest(
                "admin@example.com", "Анна Администратор", "password", AccountRole.ADMIN
        );
        when(accountRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.create(request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.fullName()).isEqualTo(request.fullName());
        assertThat(response.role()).isEqualTo(AccountRole.ADMIN);
        verify(passwordEncoder).encode(request.password());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void findsAllAccounts() {
        Account admin = new Account("admin@example.com", "Администратор", "hash", AccountRole.ADMIN);
        Account manager = new Account("manager@example.com", "Менеджер", "hash", AccountRole.MANAGER);
        when(accountRepository.findAll(any(Sort.class))).thenReturn(List.of(admin, manager));

        List<AccountResponse> response = accountService.findAll();

        assertThat(response).extracting(AccountResponse::email)
                .containsExactly("admin@example.com", "manager@example.com");
    }

    @Test
    void findsAccountById() {
        UUID id = UUID.randomUUID();
        Account account = new Account("manager@example.com", "Менеджер", "hash", AccountRole.MANAGER);
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.findById(id);

        assertThat(response.email()).isEqualTo("manager@example.com");
        assertThat(response.role()).isEqualTo(AccountRole.MANAGER);
    }

    @Test
    void updatesAccount() {
        UUID id = UUID.randomUUID();
        Account account = new Account("old@example.com", "Старое имя", "old-hash", AccountRole.MANAGER);
        UpdateAccountRequest request = new UpdateAccountRequest(
                "new@example.com", "Новое имя", "new-password", AccountRole.ADMIN
        );
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("new-hash");

        AccountResponse response = accountService.update(id, request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.fullName()).isEqualTo(request.fullName());
        assertThat(response.role()).isEqualTo(AccountRole.ADMIN);
        assertThat(account.getPasswordHash()).isEqualTo("new-hash");
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deletesAccount() {
        UUID id = UUID.randomUUID();
        Account account = new Account("manager@example.com", "Менеджер", "hash", AccountRole.MANAGER);
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        accountService.delete(id);

        verify(accountRepository).delete(account);
    }
}
