package com.example.cowmjucraft.domain.order.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class MailOutboxWorker {

    private final MailOutboxService mailOutboxService;
    private final MailOutboxDeliveryService deliveryService;

    @Value("${mail.outbox.batch-size:20}")
    private int batchSize;

    @Value("${mail.outbox.max-attempts:5}")
    private int maxAttempts;

    @Value("${mail.outbox.processing-timeout-seconds:900}")
    private long processingTimeoutSeconds;

    @Value("${mail.outbox.retry-base-seconds:10}")
    private long retryBaseSeconds;

    @Value("${mail.outbox.retry-max-seconds:300}")
    private long retryMaxSeconds;

    @Scheduled(
            fixedDelayString = "${mail.outbox.poll-interval-ms:1000}",
            initialDelayString = "${mail.outbox.initial-delay-ms:1000}"
    )
    public void processPending() {
        LocalDateTime now = LocalDateTime.now();
        List<MailOutboxMessage> messages = mailOutboxService.claimBatch(
                now,
                now.minusSeconds(processingTimeoutSeconds),
                batchSize
        );

        for (MailOutboxMessage message : messages) {
            deliver(message);
        }
    }

    private void deliver(MailOutboxMessage message) {
        try {
            deliveryService.deliver(message);
            mailOutboxService.markSent(message.id(), message.claimToken(), LocalDateTime.now());
        } catch (Exception exception) {
            long retryDelaySeconds = calculateRetryDelaySeconds(message.attemptCount());
            mailOutboxService.markDeliveryFailed(
                    message.id(),
                    message.claimToken(),
                    exception.getMessage(),
                    LocalDateTime.now().plusSeconds(retryDelaySeconds),
                    maxAttempts
            );
            log.error(
                    "Outbox 메일 발송 실패: outboxId={}, eventType={}, attempt={}",
                    message.id(),
                    message.eventType(),
                    message.attemptCount(),
                    exception
            );
        }
    }

    private long calculateRetryDelaySeconds(int attemptCount) {
        int exponent = Math.min(Math.max(attemptCount - 1, 0), 30);
        long multiplier = 1L << exponent;
        if (retryBaseSeconds > retryMaxSeconds / multiplier) {
            return retryMaxSeconds;
        }
        return Math.min(retryBaseSeconds * multiplier, retryMaxSeconds);
    }
}
