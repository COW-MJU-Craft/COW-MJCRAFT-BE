package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerOrderListItemResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 + 비밀번호로 자기 주문을 조회한다.
 *
 * <p>정보를 저장하기 전에 만든 주문도 목록에 포함된다 — 완료 메일을 그 주소가
 * 받았으므로 사실상 그 사람의 주문이다. 기존 {@code order_auth} 주문들도 V11
 * backfill에서 이메일 기준으로 연결된다.
 */
@Service
@RequiredArgsConstructor
public class CustomerOrderQueryService {

    private final CustomerCredentialService customerCredentialService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailQueryService orderDetailQueryService;

    @Transactional
    public List<CustomerOrderListItemResponseDto> getOrders(CustomerCredentialRequestDto request) {
        Customer customer = customerCredentialService.authenticate(request.email(), request.password());

        List<Order> orders = orderRepository.findAllByCustomerIdOrderByCreatedAtDescIdDesc(customer.getId());
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, List<OrderItem>> itemsByOrderId = orderItemRepository
                .findAllByOrderIdInOrderByOrderIdAndProjectItemId(orders.stream().map(Order::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return orders.stream()
                .map(order -> new CustomerOrderListItemResponseDto(
                        order.getId(),
                        order.getOrderNo(),
                        order.getStatus().name(),
                        order.getFinalAmount(),
                        order.getDepositDeadline(),
                        order.getCreatedAt(),
                        summarize(itemsByOrderId.getOrDefault(order.getId(), List.of()))
                ))
                .toList();
    }

    /**
     * 남의 주문을 요청하면 403이 아니라 <b>404</b>를 준다.
     * 403은 "그 주문이 존재한다"는 사실을 흘린다.
     */
    @Transactional
    public OrderDetailResponseDto getOrderDetail(Long orderId, CustomerCredentialRequestDto request) {
        Customer customer = customerCredentialService.authenticate(request.email(), request.password());

        Order order = orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new CustomerException(
                        CustomerErrorType.ORDER_NOT_FOUND,
                        "orderId=" + orderId + ", customerId=" + customer.getId()
                ));

        return orderDetailQueryService.getByOrderId(order.getId());
    }

    /** "머그컵 외 2건" 형태의 목록용 요약. */
    private String summarize(List<OrderItem> items) {
        if (items.isEmpty()) {
            return "";
        }
        String firstName = items.getFirst().getItemNameSnapshot();
        int rest = items.size() - 1;
        return rest == 0 ? firstName : firstName + " 외 " + rest + "건";
    }
}
