package com.provision.backend.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(10)
@ConditionalOnProperty(
        prefix = "app.bootstrap-users",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AccountBootstrapListener {
    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 20;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createRequiredAccounts() {
        createIfRoleMissing(
                AccountRole.ADMIN,
                "admin@provision.local",
                "Системный администратор"
        );
        createIfRoleMissing(
                AccountRole.MANAGER,
                "manager@provision.local",
                "Системный менеджер"
        );
    }

    private void createIfRoleMissing(AccountRole role, String email, String fullName) {
        if (accountRepository.existsByRole(role)) {
            return;
        }

        String password = generatePassword();
        Account account = new Account(email, fullName, passwordEncoder.encode(password), role);
        accountRepository.save(account);
        log.warn("Создана начальная учётная запись: role={}, email={}, password={}",
                role, email, password);
    }

    private String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int index = 0; index < PASSWORD_LENGTH; index++) {
            password.append(PASSWORD_ALPHABET.charAt(secureRandom.nextInt(PASSWORD_ALPHABET.length())));
        }
        return password.toString();
    }
}
