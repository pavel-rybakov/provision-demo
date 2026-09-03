package com.provision.backend.readingimports.api;

import com.provision.backend.readingimports.ReadingImportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReadingImportExceptionHandler {
    @ExceptionHandler(ReadingImportException.class)
    ProblemDetail handle(ReadingImportException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Импорт показаний не выполнен");
        problem.setDetail(exception.getMessage());
        return problem;
    }
}
