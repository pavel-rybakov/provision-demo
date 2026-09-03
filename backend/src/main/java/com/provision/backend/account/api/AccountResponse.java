package com.provision.backend.account.api;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountRole;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        String fullName,
        AccountRole role
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getRole()
        );
    }
}
