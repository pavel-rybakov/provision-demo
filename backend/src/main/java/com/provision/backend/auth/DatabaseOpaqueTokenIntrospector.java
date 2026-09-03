package com.provision.backend.auth;

import com.provision.backend.account.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final AuthTokenRepository authTokenRepository;
    private final TokenCodec tokenCodec;
    private final Clock clock = Clock.systemUTC();

    public DatabaseOpaqueTokenIntrospector(AuthTokenRepository authTokenRepository, TokenCodec tokenCodec) {
        this.authTokenRepository = authTokenRepository;
        this.tokenCodec = tokenCodec;
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        AuthToken authToken = authTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenCodec.hash(token), clock.instant())
                .orElseThrow(() -> new OAuth2IntrospectionException("Недействительный Bearer Token"));

        Account account = authToken.getAccount();
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + account.getRole().name())
        );
        Map<String, Object> attributes = Map.of(
                "sub", account.getId().toString(),
                "email", account.getEmail(),
                "name", account.getFullName(),
                "role", account.getRole().name()
        );

        return new DefaultOAuth2AuthenticatedPrincipal(account.getEmail(), attributes, authorities);
    }
}
