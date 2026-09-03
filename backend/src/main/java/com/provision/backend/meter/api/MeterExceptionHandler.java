package com.provision.backend.meter.api;

import com.provision.backend.meter.ElectricityMeterNotFoundException;
import com.provision.backend.meterreadings.MeterReadingNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MeterExceptionHandler {

    @ExceptionHandler({ElectricityMeterNotFoundException.class, MeterReadingNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Запись не найдена");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation() {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Конфликт данных");
        problem.setDetail("Операция нарушает уникальность или существующие связи данных");
        return problem;
    }
}
