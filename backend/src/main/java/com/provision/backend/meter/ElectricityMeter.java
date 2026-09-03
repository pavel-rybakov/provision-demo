package com.provision.backend.meter;

import com.provision.backend.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

@Entity
@Table(name = "electricity_meter")
@Getter
public class ElectricityMeter {

    @Id
    @UuidGenerator(style = VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_account_id", nullable = false, updatable = false)
    private Account createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_account_id")
    @Setter
    private Account updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    @Setter
    private String serialNumber;

    @Column(name = "inventory_number", nullable = false, unique = true, length = 100)
    @Setter
    private String inventoryNumber;

    @Column(name = "manufacture_year")
    @Setter
    private Integer manufactureYear;

    @Column(name = "transformation_ratio", precision = 19, scale = 6)
    @Setter
    private BigDecimal transformationRatio;

    @Column(name = "installation_date")
    @Setter
    private LocalDate installationDate;

    @Column(name = "seal_number", length = 100)
    @Setter
    private String sealNumber;

    @Column(name = "antimagnetic_seal_number", length = 100)
    @Setter
    private String antimagneticSealNumber;

    @Column(name = "installation_location", length = 500)
    @Setter
    private String installationLocation;

    @Column(name = "note", length = 2000)
    @Setter
    private String note;

    @Column(name = "gis_housing_id", unique = true, length = 100)
    @Setter
    private String gisHousingId;

    protected ElectricityMeter() {
    }

    public ElectricityMeter(Account createdBy, String serialNumber, String inventoryNumber) {
        this.createdBy = Objects.requireNonNull(createdBy);
        this.serialNumber = Objects.requireNonNull(serialNumber);
        this.inventoryNumber = Objects.requireNonNull(inventoryNumber);
    }
}
