package com.provision.backend.reports;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.reports.latest-readings")
public record LatestReadingsReportProperties(
        @NotBlank String cron,
        @NotBlank String zone,
        @NotBlank @Email String recipient,
        @NotBlank @Email String sender
) {
}
