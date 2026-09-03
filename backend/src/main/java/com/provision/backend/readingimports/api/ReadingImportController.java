package com.provision.backend.readingimports.api;

import com.provision.backend.readingimports.ReadingImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reading-imports")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Импорт показаний", description = "Загрузка, проверка и применение CSV-файлов с показаниями")
@RequiredArgsConstructor
public class ReadingImportController {
    private final ReadingImportService service;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Загрузить CSV в промежуточные таблицы")
    public ReadingImportResponse upload(@RequestPart("file") MultipartFile file,
                                        BearerTokenAuthentication authentication) {
        return service.upload(file, UUID.fromString(authentication.getTokenAttributes().get("sub").toString()));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Проверить подготовленные строки импорта")
    public ReadingImportResponse validate(@PathVariable UUID id) { return service.validate(id); }

    @PostMapping("/{id}/apply")
    @Operation(summary = "Атомарно применить проверенный импорт")
    public ReadingImportResponse apply(@PathVariable UUID id) { return service.apply(id); }

    @GetMapping("/{id}")
    @Operation(summary = "Получить импорт и ошибки его строк")
    public ReadingImportResponse findById(@PathVariable UUID id) { return service.findById(id); }
}
