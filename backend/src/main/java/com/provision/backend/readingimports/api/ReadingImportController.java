package com.provision.backend.readingimports.api;

import com.provision.backend.readingimports.ReadingImportService;
import com.provision.backend.readingimports.DebugReadingImportCsvGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reading-imports")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Импорт показаний", description = "Загрузка, проверка и применение CSV-файлов с показаниями")
@RequiredArgsConstructor
public class ReadingImportController {
    private final ReadingImportService service;
    private final DebugReadingImportCsvGenerator debugCsvGenerator;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Загрузить CSV-файл и запустить его фоновую обработку")
    public ReadingImportResponse importReadings(@RequestPart("file") MultipartFile file,
                                                BearerTokenAuthentication authentication) {
        return service.importReadings(
                file, UUID.fromString(authentication.getTokenAttributes().get("sub").toString()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить импорт и ошибки его строк")
    public ReadingImportResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping(value = "/debug-csv", produces = "text/csv")
    @Operation(summary = "Сгенерировать тестовый CSV для импорта")
    public ResponseEntity<StreamingResponseBody> generateDebugCsv(@RequestParam int rows) {
        StreamingResponseBody body = debugCsvGenerator.generate(rows);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reading-import-debug-" + rows + ".csv")
                .body(body);
    }
}
