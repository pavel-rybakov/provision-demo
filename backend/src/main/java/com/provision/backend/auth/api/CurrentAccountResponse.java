package com.provision.backend.auth.api;

import com.provision.backend.account.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Текущая авторизованная учётная запись")
public record CurrentAccountResponse(
        @Schema(description = "Идентификатор учётной записи") UUID id,
        @Schema(description = "Адрес электронной почты") String email,
        @Schema(description = "Фамилия, имя и отчество") String fullName,
        @Schema(description = "Роль пользователя") AccountRole role
) {
}
