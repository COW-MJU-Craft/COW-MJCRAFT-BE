package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderDetailQueryService orderDetailQueryService;
    private final OrderViewTokenService orderViewTokenService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<AdminOrderListItemResponseDto> getOrders(OrderStatus status) {
        List<Order> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findAllByStatusOrderByCreatedAtDesc(status);

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, OrderBuyer> buyerByOrderId = orderBuyerRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderBuyer::getOrderId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    OrderBuyer buyer = buyerByOrderId.get(order.getId());
                    return new AdminOrderListItemResponseDto(
                            order.getId(),
                            order.getOrderNo(),
                            order.getStatus().name(),
                            order.getFinalAmount(),
                            order.getDepositorName(),
                            buyer == null ? null : buyer.getName(),
                            buyer == null ? null : buyer.getPhone(),
                            order.getCreatedAt(),
                            order.getDepositDeadline()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderDetail(Long orderId) {
        return orderDetailQueryService.getByOrderId(orderId);
    }

    @Transactional
    public AdminOrderStatusResponseDto cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND, "orderId=" + orderId));

        OrderBuyer buyer = orderBuyerRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.BUYER_NOT_FOUND, "orderId=" + orderId));

        String normalizedReason = normalizeOptionalText(reason);
        LocalDateTime now = LocalDateTime.now();

        if (order.getStatus() == OrderStatus.PENDING_DEPOSIT) {
            OrderStatusTransitionPolicy.validate(order.getStatus(), OrderStatus.CANCELED);
            order.cancelPendingDeposit(now, normalizedReason);

            String rawToken = orderViewTokenService.rotateToken(order, now);
            emailService.sendCanceled(
                    buyer.getEmail(),
                    buyer.getName(),
                    order.getOrderNo(),
                    orderViewTokenService.buildOrderViewUrl(rawToken),
                    normalizedReason,
                    now
            );
        } else if (order.getStatus() == OrderStatus.PAID) {
            OrderStatusTransitionPolicy.validate(order.getStatus(), OrderStatus.REFUND_REQUESTED);
            order.requestRefund(now, normalizedReason);

            String rawToken = orderViewTokenService.rotateToken(order, now);
            emailService.sendRefundRequested(
                    buyer.getEmail(),
                    buyer.getName(),
                    order.getOrderNo(),
                    orderViewTokenService.buildOrderViewUrl(rawToken),
                    normalizedReason,
                    now
            );
        } else {
            throw new OrderException(
                    OrderErrorType.INVALID_STATUS_TRANSITION,
                    "current=" + order.getStatus() + ", requested=CANCELED_OR_REFUND_REQUESTED"
            );
        }

        return new AdminOrderStatusResponseDto(order.getId(), order.getStatus().name());
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
