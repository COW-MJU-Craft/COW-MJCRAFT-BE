package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.service.CustomerAccountService;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateBuyerRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateFulfillmentRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.exception.ProjectErrorType;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import com.example.cowmjucraft.global.security.PasswordPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("MMddHHmm");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;
    private final OrderAuthRepository orderAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderViewTokenService orderViewTokenService;
    private final MailOutboxService mailOutboxService;
    private final PasswordPolicy passwordPolicy;
    private final ProjectRepository projectRepository;
    private final OrderPricingService orderPricingService;
    private final CustomerAccountService customerAccountService;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {
        validateAgreements(request);

        String depositorName = normalizeRequiredText(request.depositorName(), "입금자명");

        // 조회 아이디/비밀번호는 이메일 기반 고객 식별로 대체되는 중이라 선택값이다.
        // 프론트 전환이 끝나면 이 블록과 order_auth 저장이 함께 사라진다.
        String lookupId = trimToNull(request.lookupId());
        String password = trimToNull(request.password());
        boolean legacyLookupRequested = lookupId != null || password != null;

        if (legacyLookupRequested) {
            lookupId = normalizeRequiredText(lookupId, "조회 아이디");
            password = normalizeRequiredText(password, "조회 비밀번호");

            if (!passwordPolicy.isValid(password)) {
                throw new OrderException(OrderErrorType.WEAK_PASSWORD);
            }

            if (orderAuthRepository.existsByLookupId(lookupId)) {
                throw new OrderException(OrderErrorType.DUPLICATED_LOOKUP_ID);
            }
        }

        OrderCreateFulfillmentRequestDto fulfillment = request.fulfillment();
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(request.items(), fulfillment.method());
        List<OrderPricingService.PriceLine> lines = quote.lines();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        Long representativeProjectId = lines.getFirst().projectItem().getProject().getId();
        Project representativeProject = projectRepository.findByIdForUpdate(representativeProjectId)
                .orElseThrow(() -> new ProjectException(
                        ProjectErrorType.PROJECT_NOT_FOUND,
                        "projectId=" + representativeProjectId
                ));
        long projectOrderNo = representativeProject.issueNextOrderNo();

        // 이메일만으로 고객 행을 만들거나 재사용한다.
        // 프로필과 비밀번호는 건드리지 않는다 — 주문 생성은 인증을 받지 않으므로,
        // 남의 이메일로 주문해 그 사람 정보를 덮어쓰는 경로가 되면 안 된다.
        Customer customer = customerAccountService.upsertForOrder(request.buyer().email(), now);

        boolean privacyAgreed = true;
        boolean refundAgreed = true;
        boolean cancelRiskAgreed = true;
        Order order = new Order(
                generateOrderNo(representativeProjectId, projectOrderNo, now),
                customer,
                representativeProject,
                projectOrderNo,
                OrderStatus.PENDING_DEPOSIT,
                quote.totalAmount(),
                quote.shippingFee(),
                quote.finalAmount(),
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

        String postalCode = trimToNull(fulfillment.postalCode());
        String addressLine1 = trimToNull(fulfillment.addressLine1());
        String addressLine2 = trimToNull(fulfillment.addressLine2());
        if (fulfillment.method() == OrderFulfillmentMethod.DELIVERY
                && (postalCode == null || addressLine1 == null)) {
            throw new OrderException(OrderErrorType.DELIVERY_ADDRESS_REQUIRED);
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

        if (legacyLookupRequested) {
            orderAuthRepository.save(new OrderAuth(
                    savedOrder,
                    lookupId,
                    passwordEncoder.encode(password)
            ));
        }

        String rawViewToken = orderViewTokenService.issueNewToken(savedOrder, now);

        OrderCreateBuyerRequestDto buyerForMail = request.buyer();
        String viewUrl = orderViewTokenService.buildOrderViewUrl(rawViewToken);
        mailOutboxService.enqueueOrderViewLink(
                savedOrder.getId(),
                buyerForMail.email(),
                buyerForMail.name(),
                savedOrder.getOrderNo(),
                viewUrl,
                savedOrder.getDepositDeadline()
        );

        return new OrderCreateResponseDto(
                savedOrder.getId(),
                savedOrder.getOrderNo(),
                savedOrder.getRepresentativeProject().getId(),
                savedOrder.getProjectOrderNo(),
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
            throw new OrderException(OrderErrorType.PRIVACY_AGREEMENT_REQUIRED);
        }
        if (!Boolean.TRUE.equals(request.refundAgreed())) {
            throw new OrderException(OrderErrorType.REFUND_AGREEMENT_REQUIRED);
        }
        if (!Boolean.TRUE.equals(request.cancelRiskAgreed())) {
            throw new OrderException(OrderErrorType.CANCEL_RISK_AGREEMENT_REQUIRED);
        }
    }

    private String generateOrderNo(Long projectId, long projectOrderNo, LocalDateTime now) {
        return "P" + projectId
                + "-" + projectOrderNo
                + "-" + now.format(ORDER_NO_TIME_FORMAT)
                + "-" + String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw OrderException.requiredField(OrderErrorType.REQUIRED_FIELD_MISSING, fieldName);
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

}
