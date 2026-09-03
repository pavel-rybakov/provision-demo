package com.provision.backend.meterreadings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
}
