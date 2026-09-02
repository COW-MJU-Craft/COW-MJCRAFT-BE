package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderPolicyUpdateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderPolicyResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderPolicyService {

    private final OrderPolicyRepository orderPolicyRepository;

    @Transactional(readOnly = true)
    public AdminOrderPolicyResponseDto getOrderPolicy() {
        OrderPolicy orderPolicy = orderPolicyRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_POLICY_NOT_FOUND));

        return toResponse(orderPolicy);
    }

    @Transactional
    public AdminOrderPolicyResponseDto updateOrderPolicy(AdminOrderPolicyUpdateRequestDto request) {
        OrderPolicy orderPolicy = orderPolicyRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_POLICY_NOT_FOUND));

        orderPolicy.update(request.defaultShippingFee());

        return toResponse(orderPolicy);
    }

    private AdminOrderPolicyResponseDto toResponse(OrderPolicy orderPolicy) {
        return new AdminOrderPolicyResponseDto(
                orderPolicy.getId(),
                orderPolicy.getDefaultShippingFee()
        );
    }
}
