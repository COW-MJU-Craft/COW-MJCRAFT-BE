package com.example.cowmjucraft.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerOrderListItemResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.order.OrderTestFixtures;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerOrderQueryServiceTest {

    private static final String EMAIL = "yunjin@mju.ac.kr";
    private static final String PASSWORD = "Pa55word!";
    private static final Long CUSTOMER_ID = 7L;

    @Mock
    private CustomerCredentialService customerCredentialService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderDetailQueryService orderDetailQueryService;

    private CustomerOrderQueryService customerOrderQueryService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerOrderQueryService = new CustomerOrderQueryService(
                customerCredentialService,
                orderRepository,
                orderItemRepository,
                orderDetailQueryService
        );
        customer = new Customer(EMAIL);
        ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);
        given(customerCredentialService.authenticate(EMAIL, PASSWORD)).willReturn(customer);
    }

    @Test
    void getOrders_주문있음_상품요약과함께반환() {
        // given
        Order order = order(11L, "P1-1-0914-483920", 18000);
        given(orderRepository.findAllByCustomerIdOrderByCreatedAtDescIdDesc(CUSTOMER_ID))
                .willReturn(List.of(order));
        given(orderItemRepository.findAllByOrderIdInOrderByOrderIdAndProjectItemId(List.of(11L)))
                .willReturn(List.of(
                        orderItem(order, "명지공방 머그컵"),
                        orderItem(order, "스티커 팩")
                ));

        // when
        List<CustomerOrderListItemResponseDto> result = customerOrderQueryService.getOrders(request());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().orderNo()).isEqualTo("P1-1-0914-483920");
        assertThat(result.getFirst().finalAmount()).isEqualTo(18000);
        assertThat(result.getFirst().itemSummary()).isEqualTo("명지공방 머그컵 외 1건");
    }

    @Test
    void getOrders_상품이하나면_외N건을붙이지않는다() {
        // given
        Order order = order(11L, "P1-1-0914-483920", 12000);
        given(orderRepository.findAllByCustomerIdOrderByCreatedAtDescIdDesc(CUSTOMER_ID))
                .willReturn(List.of(order));
        given(orderItemRepository.findAllByOrderIdInOrderByOrderIdAndProjectItemId(List.of(11L)))
                .willReturn(List.of(orderItem(order, "명지공방 머그컵")));

        // when
        List<CustomerOrderListItemResponseDto> result = customerOrderQueryService.getOrders(request());

        // then
        assertThat(result.getFirst().itemSummary()).isEqualTo("명지공방 머그컵");
    }

    @Test
    void getOrders_주문없음_빈목록이고상품조회를하지않는다() {
        // given
        given(orderRepository.findAllByCustomerIdOrderByCreatedAtDescIdDesc(CUSTOMER_ID))
                .willReturn(List.of());

        // when
        List<CustomerOrderListItemResponseDto> result = customerOrderQueryService.getOrders(request());

        // then
        assertThat(result).isEmpty();
        then(orderItemRepository).should(never()).findAllByOrderIdInOrderByOrderIdAndProjectItemId(any());
    }

    @Test
    void getOrderDetail_본인주문_상세반환() {
        // given
        Order order = order(11L, "P1-1-0914-483920", 18000);
        given(orderRepository.findByIdAndCustomerId(11L, CUSTOMER_ID)).willReturn(Optional.of(order));

        // when
        customerOrderQueryService.getOrderDetail(11L, request());

        // then
        then(orderDetailQueryService).should().getByOrderId(11L);
    }

    @Test
    void getOrderDetail_남의주문_403이아니라404() {
        // given — 403은 "그 주문이 존재한다"는 사실을 흘린다
        given(orderRepository.findByIdAndCustomerId(99L, CUSTOMER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerOrderQueryService.getOrderDetail(99L, request()))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.ORDER_NOT_FOUND);
    }

    private CustomerCredentialRequestDto request() {
        return new CustomerCredentialRequestDto(EMAIL, PASSWORD);
    }

    private Order order(Long id, String orderNo, int finalAmount) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                orderNo,
                customer,
                OrderTestFixtures.project(1L),
                1L,
                OrderStatus.PENDING_DEPOSIT,
                finalAmount,
                0,
                finalAmount,
                now.plusDays(1),
                "김윤진",
                true, now, true, now, true, now
        );
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private OrderItem orderItem(Order order, String itemName) {
        return new OrderItem(order, (ProjectItem) null, 1, 1000, 1000, itemName);
    }
}
