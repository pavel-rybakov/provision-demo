package com.provision.backend.account.api;

import com.provision.backend.account.AccountRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull AccountRole role
) {
}
