package com.provision.backend.meter;

import java.util.UUID;

public class ElectricityMeterNotFoundException extends RuntimeException {
    public ElectricityMeterNotFoundException(UUID id) {
        super("Прибор учёта с идентификатором %s не найден".formatted(id));
    }
}
