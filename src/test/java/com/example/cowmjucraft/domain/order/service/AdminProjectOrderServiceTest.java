package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderBulkAdvanceResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminProjectOrderStatisticsResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.repository.ProjectOrderStatisticsProjection;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminProjectOrderServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private AdminOrderPaymentService adminOrderPaymentService;

    private AdminProjectOrderService adminProjectOrderService;

    @BeforeEach
    void setUp() {
        adminProjectOrderService = new AdminProjectOrderService(
                projectRepository,
                orderRepository,
                orderItemRepository,
                orderBuyerRepository,
                adminOrderPaymentService
        );
    }

    @Test
    void getStatistics_집계결과_주문수와프로젝트상품금액반환() {
        // given
        ProjectOrderStatisticsProjection projection = org.mockito.Mockito.mock(ProjectOrderStatisticsProjection.class);
        given(projectRepository.existsById(1L)).willReturn(true);
        given(orderItemRepository.calculateProjectOrderStatistics(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.anyCollection()
        )).willReturn(projection);
        given(projection.getOrderCount()).willReturn(2L);
        given(projection.getTotalOrderAmount()).willReturn(35000L);

        // when
        AdminProjectOrderStatisticsResponseDto response = adminProjectOrderService.getStatistics(1L);

        // then
        assertThat(response.orderCount()).isEqualTo(2L);
        assertThat(response.totalOrderAmount()).isEqualTo(35000L);
    }

    @Test
    void getOrders_프로젝트와상태지정_기존목록응답형식으로반환() {
        // given
        Order order = order(10L, OrderStatus.IN_PRODUCTION);
        given(projectRepository.existsById(1L)).willReturn(true);
        given(orderRepository.findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc(
                1L,
                OrderStatus.IN_PRODUCTION
        ))
                .willReturn(List.of(order));
        given(orderBuyerRepository.findAllByOrderIdIn(List.of(10L))).willReturn(List.of());

        // when
        List<AdminOrderListItemResponseDto> response = adminProjectOrderService.getOrders(
                1L,
                OrderStatus.IN_PRODUCTION
        );

        // then
        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.orderId()).isEqualTo(10L);
            assertThat(item.status()).isEqualTo("IN_PRODUCTION");
            assertThat(item.shippingFee()).isZero();
            assertThat(item.buyerName()).isNull();
        });
    }

    @Test
    void advanceOrderStatuses_동일한결제완료주문들_제작중으로일괄변경() {
        // given
        Order first = order(10L, OrderStatus.PAID);
        Order second = order(20L, OrderStatus.PAID);
        prepareAdvance(List.of(10L, 20L), List.of(first, second));

        // when
        AdminOrderBulkAdvanceResponseDto response = adminProjectOrderService.advanceOrderStatuses(
                1L,
                List.of(20L, 10L, 10L)
        );

        // then
        assertThat(first.getStatus()).isEqualTo(OrderStatus.IN_PRODUCTION);
        assertThat(second.getStatus()).isEqualTo(OrderStatus.IN_PRODUCTION);
        assertThat(response.previousStatus()).isEqualTo("PAID");
        assertThat(response.newStatus()).isEqualTo("IN_PRODUCTION");
        assertThat(response.updatedOrderIds()).containsExactly(10L, 20L);
        verifyNoInteractions(adminOrderPaymentService);
    }

    @Test
    void advanceOrderStatuses_입금대기주문들_기존결제확정서비스로각각처리() {
        // given
        Order first = order(10L, OrderStatus.PENDING_DEPOSIT);
        Order second = order(20L, OrderStatus.PENDING_DEPOSIT);
        prepareAdvance(List.of(10L, 20L), List.of(first, second));

        // when
        AdminOrderBulkAdvanceResponseDto response = adminProjectOrderService.advanceOrderStatuses(
                1L,
                List.of(10L, 20L)
        );

        // then
        assertThat(response.newStatus()).isEqualTo("PAID");
        verify(adminOrderPaymentService).confirmPaid(10L);
        verify(adminOrderPaymentService).confirmPaid(20L);
    }

    @Test
    void advanceOrderStatuses_현재상태가서로다름_OrderException발생() {
        // given
        Order first = order(10L, OrderStatus.PAID);
        Order second = order(20L, OrderStatus.IN_PRODUCTION);
        prepareAdvance(List.of(10L, 20L), List.of(first, second));

        // when & then
        assertThatThrownBy(() -> adminProjectOrderService.advanceOrderStatuses(1L, List.of(10L, 20L)))
                .isInstanceOf(OrderException.class);
        assertThat(first.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(second.getStatus()).isEqualTo(OrderStatus.IN_PRODUCTION);
        verifyNoInteractions(adminOrderPaymentService);
    }

    @Test
    void advanceOrderStatuses_환불요청주문_OrderException발생() {
        // given
        Order order = order(10L, OrderStatus.REFUND_REQUESTED);
        prepareAdvance(List.of(10L), List.of(order));

        // when & then
        assertThatThrownBy(() -> adminProjectOrderService.advanceOrderStatuses(1L, List.of(10L)))
                .isInstanceOf(OrderException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUND_REQUESTED);
    }

    private void prepareAdvance(List<Long> orderIds, List<Order> orders) {
        given(projectRepository.existsById(1L)).willReturn(true);
        given(orderRepository.findAllByIdInForUpdate(orderIds)).willReturn(orders);
    }

    private Order order(Long id, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                "ORD-" + id,
                project(1L),
                1L,
                status,
                10000,
                0,
                10000,
                now.plusDays(1),
                "입금자",
                true,
                now,
                true,
                now,
                true,
                now
        );
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
