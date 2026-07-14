package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutbox;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.order.entity.MailOutboxStatus;
import com.example.cowmjucraft.domain.order.repository.MailOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailOutboxServiceTest {

    @Mock
    private MailOutboxRepository mailOutboxRepository;

    private MailOutboxService mailOutboxService;

    @BeforeEach
    void setUp() {
        mailOutboxService = new MailOutboxService(mailOutboxRepository);
    }

    @Test
    void enqueueOrderViewLink_주문메일요청_Outbox저장() {
        // given
        LocalDateTime deadline = LocalDateTime.of(2026, 7, 15, 23, 59);

        // when
        mailOutboxService.enqueueOrderViewLink(
                10L,
                "buyer@example.com",
                "홍길동",
                "ORD-001",
                "https://example.com/orders/view?token=token",
                deadline
        );

        // then
        ArgumentCaptor<MailOutbox> captor = ArgumentCaptor.forClass(MailOutbox.class);
        verify(mailOutboxRepository).save(captor.capture());
        MailOutbox saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(MailOutboxEventType.ORDER_VIEW_LINK);
        assertThat(saved.getAggregateId()).isEqualTo(10L);
        assertThat(saved.getAggregateVersion()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(MailOutboxStatus.PENDING);
        assertThat(saved.getDepositDeadline()).isEqualTo(deadline);
    }

    @Test
    void claimBatch_발송대기건존재_선점토큰과시도횟수설정() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 7, 14, 12, 0);
        MailOutbox outbox = outbox(now.minusMinutes(1));
        ReflectionTestUtils.setField(outbox, "id", 1L);
        when(mailOutboxRepository.findReadyForUpdate(now, now.minusMinutes(5), 20))
                .thenReturn(List.of(outbox));

        // when
        List<MailOutboxMessage> messages = mailOutboxService.claimBatch(now, now.minusMinutes(5), 20);

        // then
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().claimToken()).isNotBlank();
        assertThat(messages.getFirst().attemptCount()).isEqualTo(1);
        assertThat(outbox.getStatus()).isEqualTo(MailOutboxStatus.PROCESSING);
    }

    @Test
    void markDeliveryFailed_최대시도도달_FAILED처리() {
        // given
        MailOutbox outbox = outbox(LocalDateTime.now());
        ReflectionTestUtils.setField(outbox, "id", 1L);
        outbox.claim(LocalDateTime.now(), "claim-token");
        when(mailOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        // when
        mailOutboxService.markDeliveryFailed(
                1L,
                "claim-token",
                "smtp failure",
                LocalDateTime.now().plusSeconds(10),
                1
        );

        // then
        assertThat(outbox.getStatus()).isEqualTo(MailOutboxStatus.FAILED);
        assertThat(outbox.getLastError()).isEqualTo("smtp failure");
        assertThat(outbox.getClaimToken()).isNull();
    }

    private MailOutbox outbox(LocalDateTime now) {
        return new MailOutbox(
                MailOutboxEventType.PAID_CONFIRMED,
                10L,
                1L,
                "buyer@example.com",
                "홍길동",
                "ORD-001",
                "https://example.com/orders/view?token=token",
                null,
                now,
                null,
                now
        );
    }
}
