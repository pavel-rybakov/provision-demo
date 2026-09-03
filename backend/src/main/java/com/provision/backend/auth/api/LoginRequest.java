package com.provision.backend.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для авторизации")
public record LoginRequest(
        @Schema(description = "Адрес электронной почты", example = "manager@example.com")
        @NotBlank @Email String email,

        @Schema(description = "Пароль", example = "secret")
        @NotBlank String password
) {
}
