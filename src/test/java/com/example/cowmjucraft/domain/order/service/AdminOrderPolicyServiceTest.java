package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderPolicyUpdateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderPolicyResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderPolicyServiceTest {

    @Mock
    private OrderPolicyRepository orderPolicyRepository;

    private AdminOrderPolicyService adminOrderPolicyService;

    @BeforeEach
    void setUp() {
        adminOrderPolicyService = new AdminOrderPolicyService(orderPolicyRepository);
    }

    @Test
    void updateOrderPolicy_기존행수정_성공() {
        // given
        OrderPolicy orderPolicy = new OrderPolicy(3500);
        when(orderPolicyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(orderPolicy));

        AdminOrderPolicyUpdateRequestDto request = new AdminOrderPolicyUpdateRequestDto(4000);

        // when
        AdminOrderPolicyResponseDto response = adminOrderPolicyService.updateOrderPolicy(request);

        // then
        assertThat(response.defaultShippingFee()).isEqualTo(4000);
        assertThat(orderPolicy.getDefaultShippingFee()).isEqualTo(4000);
    }

    @Test
    void updateOrderPolicy_행없음_OrderException발생() {
        // given
        when(orderPolicyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        AdminOrderPolicyUpdateRequestDto request = new AdminOrderPolicyUpdateRequestDto(4000);

        // when & then
        assertThatThrownBy(() -> adminOrderPolicyService.updateOrderPolicy(request))
                .isInstanceOf(OrderException.class);
    }
}
