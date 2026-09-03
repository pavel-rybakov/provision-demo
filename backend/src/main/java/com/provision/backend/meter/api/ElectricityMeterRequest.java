package com.provision.backend.meter.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ElectricityMeterRequest(
        @NotBlank @Size(max = 100) String serialNumber,
        @NotBlank @Size(max = 100) String inventoryNumber,
        @Min(1800) @Max(9999) Integer manufactureYear,
        @DecimalMin(value = "0", inclusive = false) BigDecimal transformationRatio,
        LocalDate installationDate,
        @Size(max = 100) String sealNumber,
        @Size(max = 100) String antimagneticSealNumber,
        @Size(max = 500) String installationLocation,
        @Size(max = 2000) String note,
        @Size(max = 100) String gisHousingId
) {
}
