package com.provision.backend.account.api;

import com.provision.backend.account.AccountEmailAlreadyExistsException;
import com.provision.backend.account.AccountNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail handleNotFound(AccountNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Учётная запись не найдена");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    @ExceptionHandler(AccountEmailAlreadyExistsException.class)
    ProblemDetail handleEmailAlreadyExists(AccountEmailAlreadyExistsException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Email уже используется");
        problem.setDetail(exception.getMessage());
        return problem;
    }
}
