package com.provision.backend.meterreadings.api;

import com.provision.backend.meterreadings.MeterReading;
import com.provision.backend.meterreadings.MeterReadingSourceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MeterReadingResponse(
        UUID id,
        UUID electricityMeterId,
        Instant measuredAt,
        BigDecimal zoneT1,
        BigDecimal zoneT2,
        BigDecimal zoneT3,
        MeterReadingSourceType sourceType,
        Instant createdAt,
        Instant updatedAt
) {
    public static MeterReadingResponse from(MeterReading reading) {
        return new MeterReadingResponse(
                reading.getId(), reading.getElectricityMeter().getId(), reading.getMeasuredAt(),
                reading.getZoneT1(), reading.getZoneT2(), reading.getZoneT3(), reading.getSourceType(),
                reading.getCreatedAt(), reading.getUpdatedAt()
        );
    }
}
