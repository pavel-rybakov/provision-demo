package com.provision.backend.readingimports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadingImportEventListener {
    private final ReadingImportProcessingService processingService;

    @Async("readingImportExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUploaded(ReadingImportUploadedEvent event) {
        try {
            if (!processingService.startValidation(event.importId())) {
                return;
            }
            if (processingService.validate(event.importId())) {
                processingService.startApplying(event.importId());
                processingService.apply(event.importId());
            }
        } catch (Exception exception) {
            log.error("Reading import {} failed", event.importId(), exception);
            processingService.markFailed(event.importId());
        }
    }
}
