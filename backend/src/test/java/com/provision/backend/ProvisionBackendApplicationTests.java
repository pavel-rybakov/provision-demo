package com.provision.backend;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.account.AccountRole;
import com.provision.backend.auth.AuthenticationService;
import com.provision.backend.auth.DatabaseOpaqueTokenIntrospector;
import com.provision.backend.auth.api.LoginRequest;
import com.provision.backend.auth.api.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:provision;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class ProvisionBackendApplicationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DatabaseOpaqueTokenIntrospector tokenIntrospector;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void savesAccountWithUuidVersionSeven() {
        Account account = new Account(
                "manager@example.com",
                "Иван Иванов",
                "password-hash",
                AccountRole.MANAGER
        );

        Account savedAccount = accountRepository.saveAndFlush(account);

        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getId().version()).isEqualTo(7);
        assertThat(accountRepository.findByEmail("manager@example.com"))
                .contains(savedAccount);
    }

    @Test
    @Transactional
    void logsInAuthenticatesAndRevokesBearerToken() {
        accountRepository.saveAndFlush(new Account(
                "admin@example.com",
                "Анна Администратор",
                passwordEncoder.encode("secret"),
                AccountRole.ADMIN
        ));

        LoginResponse loginResponse = authenticationService.login(new LoginRequest("admin@example.com", "secret"));
        OAuth2AuthenticatedPrincipal principal = tokenIntrospector.introspect(loginResponse.accessToken());

        assertThat(loginResponse.tokenType()).isEqualTo("Bearer");
        assertThat(principal.getName()).isEqualTo("admin@example.com");
        assertThat(principal.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
        String role = principal.getAttribute("role");
        assertThat(role).isEqualTo("ADMIN");

        authenticationService.logout(loginResponse.accessToken());

        assertThatThrownBy(() -> tokenIntrospector.introspect(loginResponse.accessToken()))
                .isInstanceOf(OAuth2IntrospectionException.class);
    }
}
