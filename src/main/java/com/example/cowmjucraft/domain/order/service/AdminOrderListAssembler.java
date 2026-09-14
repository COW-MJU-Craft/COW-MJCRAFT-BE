package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전체 주문 목록과 프로젝트별 주문 목록이 같은 응답 형태를 쓰므로 조립 로직을 한 곳에 둔다.
 *
 * <p>구매자·수령 정보는 주문 건수와 무관하게 목록 단위로 한 번씩만 조회한다.
 */
@Service
@RequiredArgsConstructor
public class AdminOrderListAssembler {

    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;

    @Transactional(readOnly = true)
    public List<AdminOrderListItemResponseDto> assemble(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, OrderBuyer> buyerByOrderId = orderBuyerRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderBuyer::getOrderId, Function.identity()));
        Map<Long, OrderFulfillment> fulfillmentByOrderId = orderFulfillmentRepository
                .findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderFulfillment::getOrderId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    OrderBuyer buyer = buyerByOrderId.get(order.getId());
                    OrderFulfillment fulfillment = fulfillmentByOrderId.get(order.getId());
                    return new AdminOrderListItemResponseDto(
                            order.getId(),
                            order.getOrderNo(),
                            order.getRepresentativeProject().getId(),
                            order.getProjectOrderNo(),
                            order.getStatus().name(),
                            order.getFinalAmount(),
                            order.getShippingFee(),
                            fulfillment == null ? null : fulfillment.getMethod(),
                            order.getDepositorName(),
                            buyer == null ? null : buyer.getName(),
                            buyer == null ? null : buyer.getPhone(),
                            order.getCreatedAt(),
                            order.getDepositDeadline()
                    );
                })
                .toList();
    }
}
