package com.provision.backend.auth;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.auth.api.LoginRequest;
import com.provision.backend.auth.api.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenCodec tokenCodec;
    private final Duration tokenTtl;
    private final Clock clock;

    public AuthenticationService(
            AccountRepository accountRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenCodec tokenCodec,
            @Value("${app.security.token-ttl}") Duration tokenTtl
    ) {
        this.accountRepository = accountRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenCodec = tokenCodec;
        this.tokenTtl = tokenTtl;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Неверный email или пароль"));

        String rawToken = tokenCodec.generate();
        Instant createdAt = clock.instant();
        Instant expiresAt = createdAt.plus(tokenTtl);

        authTokenRepository.save(new AuthToken(account, tokenCodec.hash(rawToken), createdAt, expiresAt));
        return new LoginResponse(rawToken, "Bearer", expiresAt);
    }

    @Transactional
    public void logout(String rawToken) {
        authTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenCodec.hash(rawToken), clock.instant())
                .ifPresent(token -> token.setRevokedAt(clock.instant()));
    }
}
