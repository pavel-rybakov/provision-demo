package com.provision.backend.meterreadings.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateMeterReadingRequest(
        @NotNull Instant measuredAt,
        @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 6) BigDecimal zoneT1,
        @PositiveOrZero @Digits(integer = 13, fraction = 6) BigDecimal zoneT2,
        @PositiveOrZero @Digits(integer = 13, fraction = 6) BigDecimal zoneT3
) {
}
