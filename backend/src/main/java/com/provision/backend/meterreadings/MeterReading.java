package com.provision.backend.meterreadings;

import com.provision.backend.meter.ElectricityMeter;

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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

@Entity
@Table(name = "meter_reading")
@Getter
public class MeterReading {

    @Id
    @UuidGenerator(style = VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electricity_meter_id", nullable = false, updatable = false)
    private ElectricityMeter electricityMeter;

    @Column(name = "measured_at", nullable = false)
    @Setter
    private Instant measuredAt;

    @Column(name = "zone_t1", nullable = false, precision = 19, scale = 6)
    @Setter
    private BigDecimal zoneT1;

    @Column(name = "zone_t2", precision = 19, scale = 6)
    @Setter
    private BigDecimal zoneT2;

    @Column(name = "zone_t3", precision = 19, scale = 6)
    @Setter
    private BigDecimal zoneT3;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false, length = 20)
    private MeterReadingSourceType sourceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MeterReading() {
    }

    public MeterReading(ElectricityMeter electricityMeter, Instant measuredAt, BigDecimal zoneT1) {
        this(electricityMeter, measuredAt, zoneT1, MeterReadingSourceType.MANUAL);
    }

    public MeterReading(ElectricityMeter electricityMeter, Instant measuredAt, BigDecimal zoneT1,
                        MeterReadingSourceType sourceType) {
        this.electricityMeter = Objects.requireNonNull(electricityMeter);
        this.measuredAt = Objects.requireNonNull(measuredAt);
        this.zoneT1 = Objects.requireNonNull(zoneT1);
        this.sourceType = Objects.requireNonNull(sourceType);
    }
}
