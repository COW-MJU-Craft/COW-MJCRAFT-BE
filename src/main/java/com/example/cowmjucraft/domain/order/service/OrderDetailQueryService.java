package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderDetailQueryService {

    private final OrderAuthRepository orderAuthRepository;
    private final OrderViewTokenRepository orderViewTokenRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;
    private final OrderRepository orderRepository;
    private final OrderViewTokenService orderViewTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getByLookupIdAndPassword(String lookupId, String password) {
        String normalizedLookupId = normalizeRequiredText(lookupId, "lookupId");
        String normalizedPassword = normalizeRequiredText(password, "password");

        OrderAuth orderAuth = orderAuthRepository.findByLookupId(normalizedLookupId)
                .orElseThrow(this::invalidLookupCredentials);

        if (!passwordEncoder.matches(normalizedPassword, orderAuth.getPasswordHash())) {
            throw invalidLookupCredentials();
        }

        return buildOrderDetail(orderAuth.getOrder());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getByViewToken(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) {
            throw new OrderException(OrderErrorType.VIEW_TOKEN_REQUIRED);
        }

        OrderViewToken orderViewToken = orderViewTokenRepository.findByTokenHash(orderViewTokenService.hashToken(plainToken))
                .orElseThrow(() -> new OrderException(OrderErrorType.INVALID_VIEW_TOKEN));

        if (orderViewToken.getRevokedAt() != null || !orderViewToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new OrderException(OrderErrorType.EXPIRED_VIEW_TOKEN);
        }

        return buildOrderDetail(orderViewToken.getOrder());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND, "orderId=" + orderId));
        return buildOrderDetail(order);
    }

    private OrderDetailResponseDto buildOrderDetail(Order order) {
        Long orderId = order.getId();

        OrderBuyer buyer = orderBuyerRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.BUYER_NOT_FOUND));

        OrderFulfillment fulfillment = orderFulfillmentRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.FULFILLMENT_NOT_FOUND));

        List<OrderDetailResponseDto.ItemInfo> items = orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(orderId).stream()
                .map(item -> new OrderDetailResponseDto.ItemInfo(
                        item.getProjectItem().getId(),
                        item.getItemNameSnapshot(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineAmount()
                ))
                .toList();

        return new OrderDetailResponseDto(
                new OrderDetailResponseDto.OrderInfo(
                        order.getOrderNo(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getShippingFee(),
                        order.getFinalAmount(),
                        order.getDepositDeadline(),
                        order.getCreatedAt(),
                        order.getCanceledAt(),
                        order.getCancelReason(),
                        order.getRefundRequestedAt(),
                        order.getRefundedAt()
                ),
                new OrderDetailResponseDto.BuyerInfo(
                        buyer.getName(),
                        buyer.getPhone(),
                        buyer.getEmail(),
                        buyer.getBuyerType().name(),
                        buyer.getCampus(),
                        buyer.getDepartmentOrMajor(),
                        buyer.getStudentNo(),
                        buyer.getRefundBank(),
                        buyer.getRefundAccount(),
                        buyer.getReferralSource()
                ),
                new OrderDetailResponseDto.FulfillmentInfo(
                        fulfillment.getMethod().name(),
                        fulfillment.getReceiverName(),
                        fulfillment.getReceiverPhone(),
                        fulfillment.getAddressLine1(),
                        fulfillment.getAddressLine2(),
                        fulfillment.getPostalCode(),
                        fulfillment.getDeliveryMemo()
                ),
                items
        );
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new OrderException(OrderErrorType.REQUIRED_FIELD_MISSING, fieldName + "는 필수입니다.");
        }
        return value.trim();
    }

    private OrderException invalidLookupCredentials() {
        return new OrderException(OrderErrorType.INVALID_LOOKUP_CREDENTIALS);
    }
}
