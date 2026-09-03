package com.provision.backend.reports;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LatestReadingsReportJobTest {
    @Test
    void delegatesScheduledExecutionToReportService() {
        LatestReadingsReportService service = mock(LatestReadingsReportService.class);

        new LatestReadingsReportJob(service).run();

        verify(service).generateAndSend();
    }
}
