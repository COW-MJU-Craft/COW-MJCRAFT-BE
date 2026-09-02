package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderTrackingInformationResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminOrderTrackingInformationServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderFulfillmentRepository orderFulfillmentRepository;

    private AdminOrderTrackingInformationService service;

    @BeforeEach
    void setUp() {
        service = new AdminOrderTrackingInformationService(
                projectRepository,
                orderItemRepository,
                orderFulfillmentRepository
        );
    }

    @Test
    void updateTrackingInformation_공백포함운송장정보_정규화하여수정() {
        // given
        OrderFulfillment fulfillment = fulfillment(order(10L, OrderStatus.READY_TO_SHIP));
        prepareUpdate(fulfillment);

        // when
        AdminOrderTrackingInformationResponseDto response = service.updateTrackingInformation(
                1L,
                10L,
                "  CJ대한통운 1234-5678  "
        );

        // then
        assertThat(fulfillment.getTrackingInformation()).isEqualTo("CJ대한통운 1234-5678");
        assertThat(response.trackingInformation()).isEqualTo("CJ대한통운 1234-5678");
    }

    @Test
    void updateTrackingInformation_빈문자열입력_운송장정보삭제() {
        // given
        OrderFulfillment fulfillment = fulfillment(order(10L, OrderStatus.DELIVERED));
        fulfillment.updateTrackingInformation("기존 운송장");
        prepareUpdate(fulfillment);

        // when
        AdminOrderTrackingInformationResponseDto response = service.updateTrackingInformation(1L, 10L, "   ");

        // then
        assertThat(fulfillment.getTrackingInformation()).isNull();
        assertThat(response.trackingInformation()).isNull();
    }

    private void prepareUpdate(OrderFulfillment fulfillment) {
        given(projectRepository.existsById(1L)).willReturn(true);
        given(orderItemRepository.findOrderIdsByProjectIdAndOrderIdIn(1L, List.of(10L)))
                .willReturn(List.of(10L));
        given(orderFulfillmentRepository.findById(10L)).willReturn(Optional.of(fulfillment));
    }

    private Order order(Long id, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                "ORD-" + id,
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

    private OrderFulfillment fulfillment(Order order) {
        return new OrderFulfillment(
                order,
                OrderFulfillmentMethod.DELIVERY,
                "수령인",
                "010-1234-5678",
                true,
                "12345",
                "서울시",
                null,
                null
        );
    }
}
