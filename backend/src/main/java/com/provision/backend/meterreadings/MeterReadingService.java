package com.provision.backend.meterreadings;

import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterNotFoundException;
import com.provision.backend.meter.ElectricityMeterRepository;
import com.provision.backend.meterreadings.api.CreateMeterReadingRequest;
import com.provision.backend.meterreadings.api.MeterReadingResponse;
import com.provision.backend.meterreadings.api.UpdateMeterReadingRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterReadingService {
    private final MeterReadingRepository readingRepository;
    private final ElectricityMeterRepository meterRepository;

    @Transactional
    public MeterReadingResponse create(CreateMeterReadingRequest request) {
        ElectricityMeter meter = getMeter(request.electricityMeterId());
        MeterReading reading = new MeterReading(meter, request.measuredAt(), request.zoneT1());
        reading.setZoneT2(request.zoneT2());
        reading.setZoneT3(request.zoneT3());
        return MeterReadingResponse.from(readingRepository.save(reading));
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> findAll() {
        return readingRepository.findAll(Sort.by(Sort.Direction.DESC, "measuredAt")).stream()
                .map(MeterReadingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeterReadingResponse findById(UUID id) {
        return MeterReadingResponse.from(getReading(id));
    }

    @Transactional
    public MeterReadingResponse update(UUID id, UpdateMeterReadingRequest request) {
        MeterReading reading = getReading(id);
        reading.setMeasuredAt(request.measuredAt());
        reading.setZoneT1(request.zoneT1());
        reading.setZoneT2(request.zoneT2());
        reading.setZoneT3(request.zoneT3());
        return MeterReadingResponse.from(reading);
    }

    @Transactional
    public void delete(UUID id) {
        readingRepository.delete(getReading(id));
    }

    private ElectricityMeter getMeter(UUID id) {
        return meterRepository.findById(id).orElseThrow(() -> new ElectricityMeterNotFoundException(id));
    }

    private MeterReading getReading(UUID id) {
        return readingRepository.findById(id).orElseThrow(() -> new MeterReadingNotFoundException(id));
    }
}
