package com.provision.backend.meterreadings;

import java.util.UUID;

public class MeterReadingNotFoundException extends RuntimeException {
    public MeterReadingNotFoundException(UUID id) {
        super("Показание с идентификатором %s не найдено".formatted(id));
    }
}
