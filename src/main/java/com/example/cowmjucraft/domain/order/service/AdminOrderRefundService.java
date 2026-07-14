package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderRefundService {

    private final OrderRepository orderRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderViewTokenService orderViewTokenService;
    private final MailOutboxService mailOutboxService;

    @Transactional
    public AdminOrderStatusResponseDto confirmRefund(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND, "orderId=" + orderId));

        OrderStatusTransitionPolicy.validate(order.getStatus(), OrderStatus.REFUNDED);

        LocalDateTime now = LocalDateTime.now();
        order.confirmRefund(now);

        OrderBuyer buyer = orderBuyerRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.BUYER_NOT_FOUND, "orderId=" + orderId));

        String rawToken = orderViewTokenService.rotateToken(order, now);
        mailOutboxService.enqueueStatusMail(
                MailOutboxEventType.REFUNDED,
                order.getId(),
                buyer.getEmail(),
                buyer.getName(),
                order.getOrderNo(),
                orderViewTokenService.buildOrderViewUrl(rawToken),
                null,
                now
        );

        return new AdminOrderStatusResponseDto(order.getId(), order.getStatus().name());
    }
}
