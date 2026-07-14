package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailOutboxDeliveryServiceTest {

    @Mock
    private EmailService emailService;

    @Test
    void deliver_환불요청이벤트_환불요청메일발송() {
        // given
        LocalDateTime eventAt = LocalDateTime.of(2026, 7, 14, 12, 0);
        MailOutboxMessage message = new MailOutboxMessage(
                1L,
                "claim-token",
                1,
                MailOutboxEventType.REFUND_REQUESTED,
                "buyer@example.com",
                "홍길동",
                "ORD-001",
                "https://example.com/orders/view?token=token",
                "단순 변심",
                eventAt,
                null
        );
        MailOutboxDeliveryService service = new MailOutboxDeliveryService(emailService);

        // when
        service.deliver(message);

        // then
        verify(emailService).sendRefundRequested(
                "buyer@example.com",
                "홍길동",
                "ORD-001",
                "https://example.com/orders/view?token=token",
                "단순 변심",
                eventAt
        );
    }
}
