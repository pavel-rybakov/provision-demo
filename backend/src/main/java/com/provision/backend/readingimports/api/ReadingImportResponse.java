package com.provision.backend.readingimports.api;

import com.provision.backend.readingimports.ReadingImport;
import com.provision.backend.readingimports.ReadingImportRow;
import com.provision.backend.readingimports.ReadingImportStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReadingImportResponse(UUID id, String originalFilename, String fileHash,
                                    ReadingImportStatus status, int totalRows, int validRows,
                                    int invalidRows, Instant createdAt, Instant validatedAt,
                                    Instant appliedAt, List<ReadingImportRowResponse> rows) {
    public static ReadingImportResponse from(ReadingImport value, List<ReadingImportRow> rows) {
        return new ReadingImportResponse(value.getId(), value.getOriginalFilename(), value.getFileHash(),
                value.getStatus(), value.getTotalRows(), value.getValidRows(), value.getInvalidRows(),
                value.getCreatedAt(), value.getValidatedAt(), value.getAppliedAt(),
                rows.stream().map(ReadingImportRowResponse::from).toList());
    }
}
