package com.provision.backend.readingimports.api;

import com.provision.backend.readingimports.ReadingImportRow;
import java.util.UUID;

public record ReadingImportRowResponse(int rowNumber, String meterSerialNumber,
                                       UUID electricityMeterId, String validationError) {
    public static ReadingImportRowResponse from(ReadingImportRow row) {
        return new ReadingImportRowResponse(row.getRowNumber(), row.getMeterSerialNumber(),
                row.getElectricityMeter() == null ? null : row.getElectricityMeter().getId(),
                row.getValidationError());
    }
}
