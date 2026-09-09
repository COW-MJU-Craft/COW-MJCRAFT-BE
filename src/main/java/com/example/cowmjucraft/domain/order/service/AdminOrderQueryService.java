package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final AdminOrderListAssembler adminOrderListAssembler;
    private final OrderDetailQueryService orderDetailQueryService;
    private final OrderViewTokenService orderViewTokenService;
    private final MailOutboxService mailOutboxService;

    @Transactional(readOnly = true)
    public List<AdminOrderListItemResponseDto> getOrders(
            OrderStatus status,
            OrderFulfillmentMethod fulfillmentMethod
    ) {
        return adminOrderListAssembler.assemble(
                orderRepository.findAllByFilters(null, status, fulfillmentMethod)
        );
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
            mailOutboxService.enqueueStatusMail(
                    MailOutboxEventType.CANCELED,
                    order.getId(),
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
            mailOutboxService.enqueueStatusMail(
                    MailOutboxEventType.REFUND_REQUESTED,
                    order.getId(),
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
