package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.mockito.BDDMockito.given;

import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderCompletePage;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderCompletePageRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import com.example.cowmjucraft.global.security.CredentialMatcher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderDetailQueryServiceTest {

    @Mock
    private OrderAuthRepository orderAuthRepository;
    @Mock
    private OrderViewTokenRepository orderViewTokenRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private OrderFulfillmentRepository orderFulfillmentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderViewTokenService orderViewTokenService;
    @Mock
    private CredentialMatcher credentialMatcher;
    @Mock
    private OrderCompletePageRepository orderCompletePageRepository;

    @InjectMocks
    private OrderDetailQueryService orderDetailQueryService;

    @Mock
    private Order order;
    @Mock
    private OrderBuyer buyer;
    @Mock
    private OrderFulfillment fulfillment;
    @Mock
    private OrderCompletePage orderCompletePage;

    @Test
    void getByOrderId_운송장정보존재_상세응답에포함() {
        // given
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));
        given(order.getId()).willReturn(10L);
        given(order.getRepresentativeProject()).willReturn(project(1L));
        given(order.getProjectOrderNo()).willReturn(1L);
        given(order.getStatus()).willReturn(OrderStatus.READY_TO_SHIP);
        given(orderBuyerRepository.findById(10L)).willReturn(Optional.of(buyer));
        given(buyer.getBuyerType()).willReturn(OrderBuyerType.STUDENT);
        given(orderFulfillmentRepository.findById(10L)).willReturn(Optional.of(fulfillment));
        given(fulfillment.getMethod()).willReturn(OrderFulfillmentMethod.DELIVERY);
        given(fulfillment.getTrackingInformation()).willReturn("CJ대한통운 1234-5678");
        given(orderCompletePageRepository.findFirstByOrderByIdAsc()).willReturn(Optional.of(orderCompletePage));
        given(orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(10L)).willReturn(List.of());

        // when
        OrderDetailResponseDto response = orderDetailQueryService.getByOrderId(10L);

        // then
        assertThat(response.fulfillment().trackingInformation()).isEqualTo("CJ대한통운 1234-5678");
    }
}
