package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutbox;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.order.repository.MailOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailOutboxService {

    private static final long INITIAL_AGGREGATE_VERSION = 1L;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final MailOutboxRepository mailOutboxRepository;

    @Transactional
    public void enqueueOrderViewLink(
            Long orderId,
            String recipient,
            String buyerName,
            String orderNo,
            String viewUrl,
            LocalDateTime depositDeadline
    ) {
        enqueue(MailOutboxEventType.ORDER_VIEW_LINK, orderId, recipient, buyerName, orderNo, viewUrl, null, null, depositDeadline);
    }

    @Transactional
    public void enqueueStatusMail(
            MailOutboxEventType eventType,
            Long orderId,
            String recipient,
            String buyerName,
            String orderNo,
            String viewUrl,
            String reason,
            LocalDateTime eventAt
    ) {
        enqueue(eventType, orderId, recipient, buyerName, orderNo, viewUrl, reason, eventAt, null);
    }

    @Transactional
    public List<MailOutboxMessage> claimBatch(LocalDateTime now, LocalDateTime staleBefore, int batchSize) {
        return mailOutboxRepository.findReadyForUpdate(now, staleBefore, batchSize).stream()
                .map(outbox -> {
                    outbox.claim(now, UUID.randomUUID().toString());
                    return new MailOutboxMessage(outbox);
                })
                .toList();
    }

    @Transactional
    public void markSent(Long id, String claimToken, LocalDateTime now) {
        mailOutboxRepository.findById(id)
                .filter(outbox -> outbox.isClaimedBy(claimToken))
                .ifPresent(outbox -> outbox.markSent(now));
    }

    @Transactional
    public void markDeliveryFailed(
            Long id,
            String claimToken,
            String error,
            LocalDateTime nextAttemptAt,
            int maxAttempts
    ) {
        mailOutboxRepository.findById(id)
                .filter(outbox -> outbox.isClaimedBy(claimToken))
                .ifPresent(outbox -> outbox.markDeliveryFailed(truncate(error), nextAttemptAt, maxAttempts));
    }

    private void enqueue(
            MailOutboxEventType eventType,
            Long orderId,
            String recipient,
            String buyerName,
            String orderNo,
            String viewUrl,
            String reason,
            LocalDateTime eventAt,
            LocalDateTime depositDeadline
    ) {
        LocalDateTime now = LocalDateTime.now();
        mailOutboxRepository.save(new MailOutbox(
                eventType,
                orderId,
                INITIAL_AGGREGATE_VERSION,
                recipient,
                buyerName,
                orderNo,
                viewUrl,
                reason,
                eventAt,
                depositDeadline,
                now
        ));
    }

    private String truncate(String error) {
        String value = error == null ? "Unknown mail delivery error" : error;
        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }
}
