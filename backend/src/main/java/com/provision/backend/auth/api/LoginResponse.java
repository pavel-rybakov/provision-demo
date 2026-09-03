package com.provision.backend.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Выданный токен авторизации")
public record LoginResponse(
        @Schema(description = "Bearer Token") String accessToken,
        @Schema(description = "Тип токена", example = "Bearer") String tokenType,
        @Schema(description = "Дата и время окончания действия токена") Instant expiresAt
) {
}
