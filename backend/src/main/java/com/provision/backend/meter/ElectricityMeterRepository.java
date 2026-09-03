package com.provision.backend.meter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ElectricityMeterRepository extends JpaRepository<ElectricityMeter, UUID> {
    Optional<ElectricityMeter> findBySerialNumber(String serialNumber);

    Optional<ElectricityMeter> findFirstByOrderByIdAsc();
}
