package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutbox;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import java.time.LocalDateTime;

public record MailOutboxMessage(
        Long id,
        String claimToken,
        int attemptCount,
        MailOutboxEventType eventType,
        String recipient,
        String buyerName,
        String orderNo,
        String viewUrl,
        String reason,
        LocalDateTime eventAt,
        LocalDateTime depositDeadline
) {
    public MailOutboxMessage(MailOutbox outbox) {
        this(
                outbox.getId(),
                outbox.getClaimToken(),
                outbox.getAttemptCount(),
                outbox.getEventType(),
                outbox.getRecipient(),
                outbox.getBuyerName(),
                outbox.getOrderNo(),
                outbox.getViewUrl(),
                outbox.getReason(),
                outbox.getEventAt(),
                outbox.getDepositDeadline()
        );
    }
}
