package com.provision.backend.reports;

import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LatestReadingsReportService {
    private final LatestReadingsReportProperties properties;
    private final LatestReadingsCsvGenerator csvGenerator;
    private final JavaMailSender mailSender;

    public void generateAndSend() {
        Path report = csvGenerator.generate();
        try {
            send(report, properties.recipient());
        } finally {
            delete(report);
        }
    }

    public Path generate() {
        return csvGenerator.generate();
    }

    public void send(Path report, String recipient) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(properties.sender());
            helper.setTo(recipient);
            helper.setSubject("Последние показания приборов учёта");
            helper.setText("Отчёт с последними показаниями всех приборов учёта находится во вложении.");
            helper.addAttachment(fileName(), new FileSystemResource(report));

            // TODO:
            //  для больших отчётов потоково загружать CSV в object storage, сохранять metadata
            //  отчёта и отправлять URL с подписью на скачивание вместо SMTP-вложения.
            mailSender.send(message);
            log.info("Отчёт последних показаний отправлен на {}", recipient);
        } catch (Exception exception) {
            throw new LatestReadingsReportException("Не удалось отправить CSV-отчёт", exception);
        }
    }

    public void delete(Path report) {
        try {
            Files.deleteIfExists(report);
        } catch (Exception exception) {
            log.warn("Не удалось удалить временный файл отчёта {}", report, exception);
        }
    }

    public String fileName() {
        return "latest-meter-readings-%s.csv".formatted(LocalDate.now());
    }
}
