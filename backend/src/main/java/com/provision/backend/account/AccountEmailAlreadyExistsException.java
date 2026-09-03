package com.provision.backend.account;

public class AccountEmailAlreadyExistsException extends RuntimeException {

    public AccountEmailAlreadyExistsException(String email) {
        super("Учётная запись с email %s уже существует".formatted(email));
    }
}
