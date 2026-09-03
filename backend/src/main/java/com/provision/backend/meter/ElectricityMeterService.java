package com.provision.backend.meter;

import com.provision.backend.account.Account;
import com.provision.backend.account.AccountNotFoundException;
import com.provision.backend.account.AccountRepository;
import com.provision.backend.meter.api.ElectricityMeterRequest;
import com.provision.backend.meter.api.ElectricityMeterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ElectricityMeterService {
    private final ElectricityMeterRepository meterRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public ElectricityMeterResponse create(ElectricityMeterRequest request, UUID currentAccountId) {
        Account account = getAccount(currentAccountId);
        ElectricityMeter meter = new ElectricityMeter(account, request.serialNumber(), request.inventoryNumber());
        apply(meter, request);
        return ElectricityMeterResponse.from(meterRepository.save(meter));
    }

    @Transactional(readOnly = true)
    public List<ElectricityMeterResponse> findAll() {
        return meterRepository.findAll(Sort.by(Sort.Direction.ASC, "serialNumber")).stream()
                .map(ElectricityMeterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ElectricityMeterResponse findById(UUID id) {
        return ElectricityMeterResponse.from(getMeter(id));
    }

    @Transactional
    public ElectricityMeterResponse update(UUID id, ElectricityMeterRequest request, UUID currentAccountId) {
        ElectricityMeter meter = getMeter(id);
        meter.setUpdatedBy(getAccount(currentAccountId));
        apply(meter, request);
        return ElectricityMeterResponse.from(meter);
    }

    @Transactional
    public void delete(UUID id) {
        meterRepository.delete(getMeter(id));
    }

    private ElectricityMeter getMeter(UUID id) {
        return meterRepository.findById(id).orElseThrow(() -> new ElectricityMeterNotFoundException(id));
    }

    private Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void apply(ElectricityMeter meter, ElectricityMeterRequest request) {
        meter.setSerialNumber(request.serialNumber());
        meter.setInventoryNumber(request.inventoryNumber());
        meter.setManufactureYear(request.manufactureYear());
        meter.setTransformationRatio(request.transformationRatio());
        meter.setInstallationDate(request.installationDate());
        meter.setSealNumber(request.sealNumber());
        meter.setAntimagneticSealNumber(request.antimagneticSealNumber());
        meter.setInstallationLocation(request.installationLocation());
        meter.setNote(request.note());
        meter.setGisHousingId(request.gisHousingId());
    }
}
