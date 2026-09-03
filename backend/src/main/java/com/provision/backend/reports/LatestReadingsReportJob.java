package com.provision.backend.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LatestReadingsReportJob {
    private final LatestReadingsReportService reportService;

    @Scheduled(
            cron = "${app.reports.latest-readings.cron}",
            zone = "${app.reports.latest-readings.zone}"
    )
    public void run() {
        reportService.generateAndSend();
    }
}
