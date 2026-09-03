package com.provision.backend.readingimports;

import com.provision.backend.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

@Entity
@Table(name = "reading_import")
@Getter
public class ReadingImport {
    @Id @UuidGenerator(style = VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "file_hash", nullable = false, updatable = false, length = 64)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private ReadingImportStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_account_id", nullable = false, updatable = false)
    private Account uploadedBy;

    @Column(name = "total_rows", nullable = false)
    @Setter
    private int totalRows;

    @Column(name = "valid_rows", nullable = false)
    @Setter
    private int validRows;

    @Column(name = "invalid_rows", nullable = false)
    @Setter
    private int invalidRows;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "validated_at")
    @Setter
    private Instant validatedAt;

    @Column(name = "applied_at")
    @Setter
    private Instant appliedAt;

    protected ReadingImport() {}

    public ReadingImport(String originalFilename, String fileHash, Account uploadedBy) {
        this.originalFilename = Objects.requireNonNull(originalFilename);
        this.fileHash = Objects.requireNonNull(fileHash);
        this.uploadedBy = Objects.requireNonNull(uploadedBy);
        this.status = ReadingImportStatus.UPLOADED;
    }
}
