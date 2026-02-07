package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateBuyerRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateFulfillmentRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;
    private final OrderAuthRepository orderAuthRepository;
    private final ProjectItemRepository projectItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderViewTokenService orderViewTokenService;
    private final EmailService emailService;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {
        validateAgreements(request);

        String depositorName = normalizeRequiredText(request.depositorName(), "입금자명");
        String lookupId = normalizeRequiredText(request.lookupId(), "조회 아이디");
        String password = normalizeRequiredText(request.password(), "조회 비밀번호");

        if (orderAuthRepository.existsByLookupId(lookupId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 조회 아이디입니다.");
        }

        Map<Long, Integer> quantityByItemId = aggregateItemQuantities(request.items());

        int totalAmount = 0;
        List<ResolvedOrderLine> lines = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : quantityByItemId.entrySet()) {
            Long projectItemId = entry.getKey();
            int quantity = entry.getValue();

            ProjectItem projectItem = projectItemRepository.findById(projectItemId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "상품을 찾을 수 없습니다. (projectItemId=" + projectItemId + ")"
                    ));

            if (projectItem.getStatus() != ItemStatus.OPEN) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "판매 중인 상품만 주문할 수 있습니다. (projectItemId=" + projectItemId + ")"
                );
            }

            if (projectItem.getSaleType() != ItemSaleType.NORMAL) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "현재는 NORMAL 판매 상품만 주문할 수 있습니다. (projectItemId=" + projectItemId + ")"
                );
            }

            Integer stockQty = projectItem.getStockQty();
            if (stockQty == null || stockQty < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "재고가 부족하여 주문할 수 없습니다. (projectItemId=" + projectItemId + ")"
                );
            }

            int unitPrice = projectItem.getPrice();
            int lineAmount;
            try {
                lineAmount = Math.multiplyExact(unitPrice, quantity);
                totalAmount = Math.addExact(totalAmount, lineAmount);
            } catch (ArithmeticException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 금액 계산 중 오류가 발생했습니다.");
            }

            lines.add(new ResolvedOrderLine(projectItem, quantity, unitPrice, lineAmount));
        }

        int shippingFee = 0;
        int finalAmount;
        try {
            finalAmount = Math.addExact(totalAmount, shippingFee);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "최종 결제 금액 계산 중 오류가 발생했습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        boolean privacyAgreed = true;
        boolean refundAgreed = true;
        boolean cancelRiskAgreed = true;
        Order order = new Order(
                generateOrderNo(now),
                OrderStatus.PENDING_DEPOSIT,
                totalAmount,
                shippingFee,
                finalAmount,
                today.plusDays(1).atTime(23, 59, 59),
                depositorName,
                privacyAgreed,
                now,
                refundAgreed,
                now,
                cancelRiskAgreed,
                now
        );

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = lines.stream()
                .map(line -> new OrderItem(
                        savedOrder,
                        line.projectItem(),
                        line.quantity(),
                        line.unitPrice(),
                        line.lineAmount(),
                        line.projectItem().getName()
                ))
                .toList();
        orderItemRepository.saveAll(orderItems);

        OrderCreateBuyerRequestDto buyer = request.buyer();
        orderBuyerRepository.save(new OrderBuyer(
                savedOrder,
                buyer.buyerType(),
                trimToNull(buyer.campus()),
                normalizeRequiredText(buyer.name(), "주문자 이름"),
                trimToNull(buyer.departmentOrMajor()),
                trimToNull(buyer.studentNo()),
                normalizeRequiredText(buyer.phone(), "주문자 연락처"),
                normalizeRequiredText(buyer.refundBank(), "환불 은행"),
                normalizeRequiredText(buyer.refundAccount(), "환불 계좌"),
                trimToNull(buyer.referralSource()),
                normalizeRequiredText(buyer.email(), "이메일")
        ));

        OrderCreateFulfillmentRequestDto fulfillment = request.fulfillment();
        String postalCode = trimToNull(fulfillment.postalCode());
        String addressLine1 = trimToNull(fulfillment.addressLine1());
        String addressLine2 = trimToNull(fulfillment.addressLine2());
        if (fulfillment.method() == OrderFulfillmentMethod.DELIVERY
                && (postalCode == null || addressLine1 == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배송 주문은 우편번호와 기본 주소가 필수입니다.");
        }
        orderFulfillmentRepository.save(new OrderFulfillment(
                savedOrder,
                fulfillment.method(),
                normalizeRequiredText(fulfillment.receiverName(), "수령인 이름"),
                normalizeRequiredText(fulfillment.receiverPhone(), "수령인 연락처"),
                Boolean.TRUE.equals(fulfillment.infoConfirmed()),
                postalCode,
                addressLine1,
                addressLine2,
                trimToNull(fulfillment.deliveryMemo())
        ));

        orderAuthRepository.save(new OrderAuth(
                savedOrder,
                lookupId,
                passwordEncoder.encode(password)
        ));

        String rawViewToken = orderViewTokenService.issueNewToken(savedOrder, now);

        OrderCreateBuyerRequestDto buyerForMail = request.buyer();
        String viewUrl = orderViewTokenService.buildOrderViewUrl(rawViewToken);
        emailService.sendOrderViewLink(
                buyerForMail.email(),
                buyerForMail.name(),
                savedOrder.getOrderNo(),
                viewUrl,
                savedOrder.getDepositDeadline()
        );

        return new OrderCreateResponseDto(
                savedOrder.getId(),
                savedOrder.getOrderNo(),
                savedOrder.getStatus().name(),
                savedOrder.getTotalAmount(),
                savedOrder.getShippingFee(),
                savedOrder.getFinalAmount(),
                savedOrder.getDepositDeadline(),
                lookupId,
                rawViewToken
        );
    }

    private void validateAgreements(OrderCreateRequestDto request) {
        if (!Boolean.TRUE.equals(request.privacyAgreed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개인정보 수집 및 이용 동의가 필요합니다.");
        }
        if (!Boolean.TRUE.equals(request.refundAgreed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "환불 정책 동의가 필요합니다.");
        }
        if (!Boolean.TRUE.equals(request.cancelRiskAgreed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 취소 리스크 동의가 필요합니다.");
        }
    }

    private Map<Long, Integer> aggregateItemQuantities(List<OrderCreateItemRequestDto> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 상품은 1개 이상이어야 합니다.");
        }

        Map<Long, Integer> quantityByItemId = new LinkedHashMap<>();
        for (OrderCreateItemRequestDto item : items) {
            if (item == null || item.projectItemId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품 정보가 올바르지 않습니다.");
            }
            if (item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다.");
            }
            quantityByItemId.merge(item.projectItemId(), item.quantity(), Math::addExact);
        }
        return quantityByItemId;
    }

    private String generateOrderNo(LocalDateTime now) {
        for (int i = 0; i < 20; i++) {
            String candidate = "ORD-" + now.format(ORDER_NO_TIME_FORMAT)
                    + "-" + String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
            if (!orderRepository.existsByOrderNo(candidate)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 번호 생성에 실패했습니다.");
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "은(는) 필수입니다.");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ResolvedOrderLine(ProjectItem projectItem, int quantity, int unitPrice, int lineAmount) {
    }
}
