package com.provision.backend.meter.api;

import com.provision.backend.meter.ElectricityMeterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
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
@RequestMapping("/api/v1/admin/meters")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Приборы учёта", description = "Административное управление приборами учёта")
@RequiredArgsConstructor
public class AdminElectricityMeterController {
    private final ElectricityMeterService meterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать прибор учёта")
    @ApiResponse(responseCode = "201", description = "Прибор учёта создан")
    public ElectricityMeterResponse create(@Valid @RequestBody ElectricityMeterRequest request,
                                           BearerTokenAuthentication authentication) {
        return meterService.create(request, currentAccountId(authentication));
    }

    @GetMapping
    @Operation(summary = "Получить список приборов учёта")
    public List<ElectricityMeterResponse> findAll() {
        return meterService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить прибор учёта")
    public ElectricityMeterResponse findById(@PathVariable UUID id) {
        return meterService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить прибор учёта")
    public ElectricityMeterResponse update(@PathVariable UUID id,
                                               @Valid @RequestBody ElectricityMeterRequest request,
                                               BearerTokenAuthentication authentication) {
        return meterService.update(id, request, currentAccountId(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить прибор учёта")
    @ApiResponse(responseCode = "204", description = "Прибор учёта удалён")
    public void delete(@PathVariable UUID id) {
        meterService.delete(id);
    }

    private UUID currentAccountId(BearerTokenAuthentication authentication) {
        return UUID.fromString(authentication.getTokenAttributes().get("sub").toString());
    }
}
