package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerPrefillResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문서 "저장한 정보 불러오기".
 *
 * <p>우선순위는 ① 직접 저장한 프로필 → ② 최근 주문 스냅샷 → ③ 없음이다.
 *
 * <p>환불 은행/계좌·입금자명·수령지는 우선순위와 무관하게 <b>항상 최근 주문
 * 스냅샷</b>에서 온다. {@code customers}에 그 컬럼이 없기 때문이다. 인증을 통과한
 * 사람은 주문 상세로 이미 그 값을 볼 수 있으므로 폼에 채우는 것이 새로운 노출은
 * 아니고, 저장 위치를 늘리지 않으면서 입력 부담만 없앤다.
 */
@Service
@RequiredArgsConstructor
public class CustomerPrefillService {

    private static final String SOURCE_PROFILE = "PROFILE";
    private static final String SOURCE_LAST_ORDER = "LAST_ORDER";

    private final CustomerCredentialService customerCredentialService;
    private final OrderRepository orderRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;

    @Transactional
    public CustomerPrefillResponseDto prefill(CustomerCredentialRequestDto request) {
        Customer customer = customerCredentialService.authenticate(request.email(), request.password());

        Optional<Order> lastOrder = orderRepository
                .findFirstByCustomerIdOrderByCreatedAtDescIdDesc(customer.getId());

        OrderBuyer lastBuyer = lastOrder
                .flatMap(order -> orderBuyerRepository.findById(order.getId()))
                .orElse(null);
        OrderFulfillment lastFulfillment = lastOrder
                .flatMap(order -> orderFulfillmentRepository.findById(order.getId()))
                .orElse(null);

        String source = resolveSource(customer, lastBuyer);
        if (source == null) {
            return new CustomerPrefillResponseDto(null, null, null);
        }

        return new CustomerPrefillResponseDto(
                source,
                buildBuyer(customer, lastBuyer, lastOrder.orElse(null), SOURCE_PROFILE.equals(source)),
                buildFulfillment(lastFulfillment)
        );
    }

    private String resolveSource(Customer customer, OrderBuyer lastBuyer) {
        if (customer.hasSavedProfile()) {
            return SOURCE_PROFILE;
        }
        return lastBuyer == null ? null : SOURCE_LAST_ORDER;
    }

    private CustomerPrefillResponseDto.BuyerPrefill buildBuyer(
            Customer customer,
            OrderBuyer lastBuyer,
            Order lastOrder,
            boolean fromProfile
    ) {
        return new CustomerPrefillResponseDto.BuyerPrefill(
                fromProfile ? customer.getName() : valueOf(lastBuyer, OrderBuyer::getName),
                fromProfile ? customer.getPhone() : valueOf(lastBuyer, OrderBuyer::getPhone),
                customer.getEmail(),
                fromProfile
                        ? nameOf(customer.getBuyerType())
                        : (lastBuyer == null ? null : nameOf(lastBuyer.getBuyerType())),
                fromProfile ? customer.getCampus() : valueOf(lastBuyer, OrderBuyer::getCampus),
                fromProfile ? customer.getDepartmentOrMajor() : valueOf(lastBuyer, OrderBuyer::getDepartmentOrMajor),
                fromProfile ? customer.getStudentNo() : valueOf(lastBuyer, OrderBuyer::getStudentNo),
                // 아래 네 값은 프로필에 존재하지 않으므로 항상 주문 스냅샷에서 온다.
                valueOf(lastBuyer, OrderBuyer::getRefundBank),
                valueOf(lastBuyer, OrderBuyer::getRefundAccount),
                valueOf(lastBuyer, OrderBuyer::getReferralSource),
                lastOrder == null ? null : lastOrder.getDepositorName()
        );
    }

    private CustomerPrefillResponseDto.FulfillmentPrefill buildFulfillment(OrderFulfillment fulfillment) {
        if (fulfillment == null) {
            return null;
        }
        return new CustomerPrefillResponseDto.FulfillmentPrefill(
                fulfillment.getMethod() == null ? null : fulfillment.getMethod().name(),
                fulfillment.getReceiverName(),
                fulfillment.getReceiverPhone(),
                fulfillment.getPostalCode(),
                fulfillment.getAddressLine1(),
                fulfillment.getAddressLine2()
        );
    }

    private String valueOf(OrderBuyer buyer, java.util.function.Function<OrderBuyer, String> getter) {
        return buyer == null ? null : getter.apply(buyer);
    }

    private String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
