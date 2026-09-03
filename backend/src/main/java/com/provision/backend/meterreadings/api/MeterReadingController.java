package com.provision.backend.meterreadings.api;

import com.provision.backend.meterreadings.MeterReadingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meter-readings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Показания", description = "Управление показаниями приборов учёта")
@RequiredArgsConstructor
public class MeterReadingController {
    private final MeterReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать показание")
    @ApiResponse(responseCode = "201", description = "Показание создано")
    public MeterReadingResponse create(@Valid @RequestBody CreateMeterReadingRequest request) {
        return readingService.create(request);
    }

    @GetMapping
    @Operation(summary = "Получить список показаний")
    public List<MeterReadingResponse> findAll() {
        return readingService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить показание")
    public MeterReadingResponse findById(@PathVariable UUID id) {
        return readingService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить показание")
    public MeterReadingResponse update(@PathVariable UUID id,
                                       @Valid @RequestBody UpdateMeterReadingRequest request) {
        return readingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить показание")
    @ApiResponse(responseCode = "204", description = "Показание удалено")
    public void delete(@PathVariable UUID id) {
        readingService.delete(id);
    }
}
