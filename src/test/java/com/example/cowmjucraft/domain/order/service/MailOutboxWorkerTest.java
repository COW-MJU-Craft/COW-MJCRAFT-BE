package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailOutboxWorkerTest {

    @Mock
    private MailOutboxService mailOutboxService;
    @Mock
    private MailOutboxDeliveryService deliveryService;

    private MailOutboxWorker worker;

    @BeforeEach
    void setUp() {
        worker = new MailOutboxWorker(mailOutboxService, deliveryService);
        ReflectionTestUtils.setField(worker, "batchSize", 20);
        ReflectionTestUtils.setField(worker, "maxAttempts", 5);
        ReflectionTestUtils.setField(worker, "processingTimeoutSeconds", 300L);
        ReflectionTestUtils.setField(worker, "retryBaseSeconds", 10L);
        ReflectionTestUtils.setField(worker, "retryMaxSeconds", 300L);
    }

    @Test
    void processPending_메일발송성공_SENT처리() {
        // given
        MailOutboxMessage message = message();
        when(mailOutboxService.claimBatch(any(), any(), eq(20))).thenReturn(List.of(message));

        // when
        worker.processPending();

        // then
        verify(deliveryService).deliver(message);
        verify(mailOutboxService).markSent(eq(1L), eq("claim-token"), any());
        verify(mailOutboxService, never()).markDeliveryFailed(any(), any(), any(), any(), any(Integer.class));
    }

    @Test
    void processPending_SMTP실패_재시도예약() {
        // given
        MailOutboxMessage message = message();
        when(mailOutboxService.claimBatch(any(), any(), eq(20))).thenReturn(List.of(message));
        doThrow(new RuntimeException("smtp timeout")).when(deliveryService).deliver(message);

        // when
        worker.processPending();

        // then
        verify(mailOutboxService, never()).markSent(any(), any(), any());
        verify(mailOutboxService).markDeliveryFailed(
                eq(1L), eq("claim-token"), eq("smtp timeout"), any(), eq(5));
    }

    private MailOutboxMessage message() {
        return new MailOutboxMessage(
                1L,
                "claim-token",
                1,
                MailOutboxEventType.PAID_CONFIRMED,
                "buyer@example.com",
                "홍길동",
                "ORD-001",
                "https://example.com/orders/view?token=token",
                null,
                LocalDateTime.now(),
                null
        );
    }
}
