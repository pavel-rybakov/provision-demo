package com.provision.backend.readingimports;

import com.provision.backend.meter.ElectricityMeter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

@Entity
@Table(name = "reading_import_row")
@Getter
public class ReadingImportRow {
    @Id @UuidGenerator(style = VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reading_import_id", nullable = false, updatable = false)
    private ReadingImport readingImport;

    @Column(name = "row_number", nullable = false) private int rowNumber;
    @Column(name = "meter_serial_number", nullable = false, length = 100) private String meterSerialNumber;
    @Column(name = "measured_at") private Instant measuredAt;
    @Column(name = "zone_t1", precision = 19, scale = 6) private BigDecimal zoneT1;
    @Column(name = "zone_t2", precision = 19, scale = 6) private BigDecimal zoneT2;
    @Column(name = "zone_t3", precision = 19, scale = 6) private BigDecimal zoneT3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "electricity_meter_id")
    @Setter
    private ElectricityMeter electricityMeter;

    @Column(name = "validation_error", length = 2000)
    @Setter
    private String validationError;

    protected ReadingImportRow() {}

    public ReadingImportRow(ReadingImport readingImport, int rowNumber, String meterSerialNumber,
                            Instant measuredAt, BigDecimal zoneT1, BigDecimal zoneT2, BigDecimal zoneT3) {
        this.readingImport = readingImport;
        this.rowNumber = rowNumber;
        this.meterSerialNumber = meterSerialNumber;
        this.measuredAt = measuredAt;
        this.zoneT1 = zoneT1;
        this.zoneT2 = zoneT2;
        this.zoneT3 = zoneT3;
    }
}
