package com.provision.backend.meter.api;

import com.provision.backend.meter.ElectricityMeter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ElectricityMeterResponse(
        UUID id,
        UUID createdByAccountId,
        UUID updatedByAccountId,
        Instant createdAt,
        Instant updatedAt,
        String serialNumber,
        String inventoryNumber,
        Integer manufactureYear,
        BigDecimal transformationRatio,
        LocalDate installationDate,
        String sealNumber,
        String antimagneticSealNumber,
        String installationLocation,
        String note,
        String gisHousingId
) {
    public static ElectricityMeterResponse from(ElectricityMeter meter) {
        return new ElectricityMeterResponse(
                meter.getId(), meter.getCreatedBy().getId(),
                meter.getUpdatedBy() == null ? null : meter.getUpdatedBy().getId(),
                meter.getCreatedAt(), meter.getUpdatedAt(), meter.getSerialNumber(),
                meter.getInventoryNumber(), meter.getManufactureYear(), meter.getTransformationRatio(),
                meter.getInstallationDate(), meter.getSealNumber(), meter.getAntimagneticSealNumber(),
                meter.getInstallationLocation(), meter.getNote(), meter.getGisHousingId()
        );
    }
}
