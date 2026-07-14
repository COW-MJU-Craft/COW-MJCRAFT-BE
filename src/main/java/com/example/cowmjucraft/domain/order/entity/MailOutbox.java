package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "mail_outboxes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mail_outboxes_event_aggregate_version",
                columnNames = {"event_type", "aggregate_id", "aggregate_version"}
        )
)
public class MailOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private MailOutboxEventType eventType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "aggregate_version", nullable = false)
    private long aggregateVersion;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(name = "buyer_name", nullable = false, length = 100)
    private String buyerName;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "view_url", nullable = false, length = 1000)
    private String viewUrl;

    @Column(length = 500)
    private String reason;

    @Column(name = "event_at")
    private LocalDateTime eventAt;

    @Column(name = "deposit_deadline")
    private LocalDateTime depositDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MailOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "claim_token", length = 36)
    private String claimToken;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public MailOutbox(
            MailOutboxEventType eventType,
            Long aggregateId,
            long aggregateVersion,
            String recipient,
            String buyerName,
            String orderNo,
            String viewUrl,
            String reason,
            LocalDateTime eventAt,
            LocalDateTime depositDeadline,
            LocalDateTime now
    ) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.recipient = recipient;
        this.buyerName = buyerName;
        this.orderNo = orderNo;
        this.viewUrl = viewUrl;
        this.reason = reason;
        this.eventAt = eventAt;
        this.depositDeadline = depositDeadline;
        this.status = MailOutboxStatus.PENDING;
        this.nextAttemptAt = now;
    }

    public void claim(LocalDateTime now, String claimToken) {
        this.status = MailOutboxStatus.PROCESSING;
        this.attemptCount++;
        this.claimedAt = now;
        this.claimToken = claimToken;
    }

    public boolean isClaimedBy(String claimToken) {
        return this.status == MailOutboxStatus.PROCESSING && Objects.equals(this.claimToken, claimToken);
    }

    public void markSent(LocalDateTime now) {
        this.status = MailOutboxStatus.SENT;
        this.sentAt = now;
        this.claimedAt = null;
        this.claimToken = null;
        this.lastError = null;
    }

    public void markDeliveryFailed(String error, LocalDateTime nextAttemptAt, int maxAttempts) {
        this.status = attemptCount >= maxAttempts ? MailOutboxStatus.FAILED : MailOutboxStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
        this.claimedAt = null;
        this.claimToken = null;
        this.lastError = error;
    }
}
