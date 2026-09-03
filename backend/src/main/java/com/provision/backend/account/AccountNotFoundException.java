package com.provision.backend.account;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Учётная запись с идентификатором %s не найдена".formatted(id));
    }
}
