package com.provision.backend.reports.api;

import com.provision.backend.reports.LatestReadingsReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Validated
@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Отчёты", description = "Формирование отчётов по приборам учёта")
@RequiredArgsConstructor
public class AdminLatestReadingsReportController {
    private final LatestReadingsReportService reportService;

    @PostMapping(value = "/latest-readings", produces = "text/csv")
    @Operation(summary = "Сформировать отчёт с последними показаниями всех приборов")
    public ResponseEntity<StreamingResponseBody> generate(
            @Parameter(description = "Дополнительно отправить отчёт на этот email")
            @RequestParam(name = "send_to_email", required = false) @Email String sendToEmail
    ) {
        Path report = reportService.generate();
        try {
            if (sendToEmail != null) {
                reportService.send(report, sendToEmail);
            }
        } catch (RuntimeException exception) {
            reportService.delete(report);
            throw exception;
        }

        StreamingResponseBody body = outputStream -> {
            try (InputStream inputStream = Files.newInputStream(report)) {
                inputStream.transferTo(outputStream);
            } finally {
                reportService.delete(report);
            }
        };
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(reportService.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }
}
