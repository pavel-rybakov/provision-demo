package com.provision.backend.reports.api;

import com.provision.backend.reports.LatestReadingsReportService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLatestReadingsReportControllerTest {
    @Test
    void returnsGeneratedCsvWithoutSendingEmail() throws Exception {
        LatestReadingsReportService service = mock(LatestReadingsReportService.class);
        Path report = Files.createTempFile("report-test-", ".csv");
        Files.writeString(report, "header\nvalue\n");
        when(service.generate()).thenReturn(report);
        when(service.fileName()).thenReturn("report.csv");
        AdminLatestReadingsReportController controller = new AdminLatestReadingsReportController(service);

        ResponseEntity<StreamingResponseBody> response = controller.generate(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        assertThat(output.toString()).isEqualTo("header\nvalue\n");
        verify(service, never()).send(any(), any());
        verify(service).delete(report);
    }

    @Test
    void sendsGeneratedCsvToRequestedEmailAndReturnsIt() throws Exception {
        LatestReadingsReportService service = mock(LatestReadingsReportService.class);
        Path report = Files.createTempFile("report-test-", ".csv");
        Files.writeString(report, "header\nvalue\n");
        when(service.generate()).thenReturn(report);
        when(service.fileName()).thenReturn("report.csv");
        AdminLatestReadingsReportController controller = new AdminLatestReadingsReportController(service);

        ResponseEntity<StreamingResponseBody> response = controller.generate("report@example.com");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        verify(service).send(report, "report@example.com");
        assertThat(output.toString()).contains("value");
        verify(service).delete(report);
    }
}
