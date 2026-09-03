package com.provision.backend.account;

import com.provision.backend.account.api.AccountResponse;
import com.provision.backend.account.api.CreateAccountRequest;
import com.provision.backend.account.api.UpdateAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        ensureEmailIsAvailable(request.email());
        Account account = new Account(
                request.email(),
                request.fullName(),
                passwordEncoder.encode(request.password()),
                request.role()
        );
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll(Sort.by(Sort.Direction.ASC, "email")).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(getAccount(id));
    }

    @Transactional
    public AccountResponse update(UUID id, UpdateAccountRequest request) {
        Account account = getAccount(id);
        if (!account.getEmail().equals(request.email())) {
            ensureEmailIsAvailable(request.email());
        }
        account.setEmail(request.email());
        account.setFullName(request.fullName());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(request.role());
        return AccountResponse.from(account);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = getAccount(id);
        accountRepository.delete(account);
    }

    private Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void ensureEmailIsAvailable(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new AccountEmailAlreadyExistsException(email);
        }
    }
}
