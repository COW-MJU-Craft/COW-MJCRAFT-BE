package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private OrderDetailQueryService orderDetailQueryService;
    @Mock
    private OrderViewTokenService orderViewTokenService;
    @Mock
    private MailOutboxService mailOutboxService;

    private AdminOrderQueryService adminOrderQueryService;

    @BeforeEach
    void setUp() {
        adminOrderQueryService = new AdminOrderQueryService(
                orderRepository,
                orderBuyerRepository,
                orderDetailQueryService,
                orderViewTokenService,
                mailOutboxService
        );
    }

    @Test
    void cancelOrder_입금대기주문_CANCELED메일적재() {
        // given
        Order order = order(OrderStatus.PENDING_DEPOSIT);
        prepare(order);

        // when
        adminOrderQueryService.cancelOrder(10L, " 단순 변심 ");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(mailOutboxService).enqueueStatusMail(
                eq(MailOutboxEventType.CANCELED),
                eq(10L),
                eq("buyer@example.com"),
                eq("홍길동"),
                eq("ORD-001"),
                eq("https://example.com/orders/view?token=token"),
                eq("단순 변심"),
                any()
        );
    }

    @Test
    void cancelOrder_결제완료주문_REFUND_REQUESTED메일적재() {
        // given
        Order order = order(OrderStatus.PAID);
        prepare(order);

        // when
        adminOrderQueryService.cancelOrder(10L, "환불 요청");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUND_REQUESTED);
        verify(mailOutboxService).enqueueStatusMail(
                eq(MailOutboxEventType.REFUND_REQUESTED),
                eq(10L),
                eq("buyer@example.com"),
                eq("홍길동"),
                eq("ORD-001"),
                eq("https://example.com/orders/view?token=token"),
                eq("환불 요청"),
                any()
        );
    }

    private void prepare(Order order) {
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(orderBuyerRepository.findById(10L)).thenReturn(Optional.of(buyer(order)));
        when(orderViewTokenService.rotateToken(eq(order), any())).thenReturn("token");
        when(orderViewTokenService.buildOrderViewUrl("token"))
                .thenReturn("https://example.com/orders/view?token=token");
    }

    private Order order(OrderStatus status) {
        Order order = new Order(
                "ORD-001",
                status,
                10_000,
                0,
                10_000,
                LocalDateTime.now().plusDays(1),
                "홍길동",
                true,
                LocalDateTime.now(),
                true,
                LocalDateTime.now(),
                true,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(order, "id", 10L);
        return order;
    }

    private OrderBuyer buyer(Order order) {
        return new OrderBuyer(
                order,
                OrderBuyerType.STUDENT,
                "SEOUL",
                "홍길동",
                "컴퓨터공학과",
                "60123456",
                "010-1234-5678",
                "국민은행",
                "123456-78-901234",
                "instagram",
                "buyer@example.com"
        );
    }
}
