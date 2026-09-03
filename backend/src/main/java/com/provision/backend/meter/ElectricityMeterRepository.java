package com.provision.backend.meter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;

public interface ElectricityMeterRepository extends JpaRepository<ElectricityMeter, UUID> {
    Optional<ElectricityMeter> findBySerialNumber(String serialNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ElectricityMeter m where m.id in :ids order by m.id")
    List<ElectricityMeter> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
