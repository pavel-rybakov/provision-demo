package com.provision.backend.auth.api;

import com.provision.backend.auth.AuthenticationService;
import com.provision.backend.account.AccountRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Авторизация", description = "Получение и отзыв Bearer Token")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизоваться", description = "Проверяет email и пароль и выдаёт Bearer Token")
    @ApiResponse(responseCode = "200", description = "Авторизация выполнена")
    @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Получить текущего пользователя",
            description = "Возвращает профиль и роль владельца текущего Bearer Token")
    @ApiResponse(responseCode = "200", description = "Текущий пользователь получен")
    @ApiResponse(responseCode = "401", description = "Bearer Token отсутствует или недействителен")
    public CurrentAccountResponse me(BearerTokenAuthentication authentication) {
        return new CurrentAccountResponse(
                UUID.fromString(authentication.getTokenAttributes().get("sub").toString()),
                authentication.getTokenAttributes().get("email").toString(),
                authentication.getTokenAttributes().get("name").toString(),
                AccountRole.valueOf(authentication.getTokenAttributes().get("role").toString())
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Завершить сеанс", description = "Отзывает текущий Bearer Token")
    @ApiResponse(responseCode = "204", description = "Токен отозван")
    @ApiResponse(responseCode = "401", description = "Bearer Token отсутствует или недействителен")
    public ResponseEntity<Void> logout(BearerTokenAuthentication authentication) {
        authenticationService.logout(authentication.getToken().getTokenValue());
        return ResponseEntity.noContent().build();
    }
}
