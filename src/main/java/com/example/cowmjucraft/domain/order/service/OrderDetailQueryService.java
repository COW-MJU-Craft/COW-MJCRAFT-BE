package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token이 필요합니다.");
        }

        OrderViewToken orderViewToken = orderViewTokenRepository.findByTokenHash(orderViewTokenService.hashToken(plainToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 조회 링크입니다."));

        if (orderViewToken.getRevokedAt() != null || !orderViewToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "만료되었거나 폐기된 조회 링크입니다.");
        }

        return buildOrderDetail(orderViewToken.getOrder());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다. (orderId=" + orderId + ")"));
        return buildOrderDetail(order);
    }

    private OrderDetailResponseDto buildOrderDetail(Order order) {
        Long orderId = order.getId();

        OrderBuyer buyer = orderBuyerRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문자 정보를 찾을 수 없습니다."));

        OrderFulfillment fulfillment = orderFulfillmentRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수령 정보를 찾을 수 없습니다."));

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "는 필수입니다.");
        }
        return value.trim();
    }

    private ResponseStatusException invalidLookupCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "조회 아이디 또는 비밀번호가 올바르지 않습니다.");
    }

}
