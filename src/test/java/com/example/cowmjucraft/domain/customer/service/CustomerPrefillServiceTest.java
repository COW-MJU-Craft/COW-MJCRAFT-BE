package com.example.cowmjucraft.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerPrefillResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.OrderTestFixtures;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerPrefillServiceTest {

    private static final String EMAIL = "yunjin@mju.ac.kr";
    private static final String PASSWORD = "Pa55word!";
    private static final Long CUSTOMER_ID = 7L;
    private static final Long ORDER_ID = 42L;

    @Mock
    private CustomerCredentialService customerCredentialService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderBuyerRepository orderBuyerRepository;

    @Mock
    private OrderFulfillmentRepository orderFulfillmentRepository;

    private CustomerPrefillService customerPrefillService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerPrefillService = new CustomerPrefillService(
                customerCredentialService,
                orderRepository,
                orderBuyerRepository,
                orderFulfillmentRepository
        );
        customer = new Customer(EMAIL);
        ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);
        given(customerCredentialService.authenticate(EMAIL, PASSWORD)).willReturn(customer);
    }

    @Test
    void prefill_저장한프로필있음_PROFILE에서기본정보를가져온다() {
        // given
        customer.updateProfile("김윤진", "01023456789", OrderBuyerType.STUDENT, "SEOUL", "융합소프트웨어학부", "60201234", LocalDateTime.now());
        givenLastOrder(order(), buyer("이전이름", "01099998888"), fulfillment());

        // when
        CustomerPrefillResponseDto result = customerPrefillService.prefill(request());

        // then — 이름·연락처는 프로필이 이긴다
        assertThat(result.source()).isEqualTo("PROFILE");
        assertThat(result.buyer().name()).isEqualTo("김윤진");
        assertThat(result.buyer().phone()).isEqualTo("01023456789");
        assertThat(result.buyer().studentNo()).isEqualTo("60201234");
    }

    @Test
    void prefill_프로필있어도_환불계좌는최근주문스냅샷에서온다() {
        // given — customers에는 환불 계좌 컬럼 자체가 없다
        customer.updateProfile("김윤진", "01023456789", OrderBuyerType.STUDENT, "SEOUL", "학부", "60201234", LocalDateTime.now());
        givenLastOrder(order(), buyer("이전이름", "01099998888"), fulfillment());

        // when
        CustomerPrefillResponseDto result = customerPrefillService.prefill(request());

        // then
        assertThat(result.buyer().refundBank()).isEqualTo("국민은행");
        assertThat(result.buyer().refundAccount()).isEqualTo("12345601234567");
        assertThat(result.buyer().depositorName()).isEqualTo("김윤진");
    }

    @Test
    void prefill_프로필없고주문있음_LAST_ORDER스냅샷을그대로쓴다() {
        // given
        givenLastOrder(order(), buyer("이전이름", "01099998888"), fulfillment());

        // when
        CustomerPrefillResponseDto result = customerPrefillService.prefill(request());

        // then
        assertThat(result.source()).isEqualTo("LAST_ORDER");
        assertThat(result.buyer().name()).isEqualTo("이전이름");
        assertThat(result.buyer().phone()).isEqualTo("01099998888");
        assertThat(result.buyer().email()).isEqualTo(EMAIL);
    }

    @Test
    void prefill_수령정보는_항상최근주문에서온다() {
        // given
        customer.updateProfile("김윤진", "01023456789", OrderBuyerType.STUDENT, "SEOUL", "학부", "60201234", LocalDateTime.now());
        givenLastOrder(order(), buyer("이전이름", "01099998888"), fulfillment());

        // when
        CustomerPrefillResponseDto result = customerPrefillService.prefill(request());

        // then — 프로필에는 주소가 없다
        assertThat(result.fulfillment().method()).isEqualTo("DELIVERY");
        assertThat(result.fulfillment().postalCode()).isEqualTo("03924");
        assertThat(result.fulfillment().addressLine1()).isEqualTo("서울 마포구 백범로 35");
    }

    @Test
    void prefill_프로필도주문도없음_모두null() {
        // given
        given(orderRepository.findFirstByCustomerIdOrderByCreatedAtDescIdDesc(CUSTOMER_ID))
                .willReturn(Optional.empty());

        // when
        CustomerPrefillResponseDto result = customerPrefillService.prefill(request());

        // then
        assertThat(result.source()).isNull();
        assertThat(result.buyer()).isNull();
        assertThat(result.fulfillment()).isNull();
    }

    private void givenLastOrder(Order order, OrderBuyer buyer, OrderFulfillment fulfillment) {
        given(orderRepository.findFirstByCustomerIdOrderByCreatedAtDescIdDesc(CUSTOMER_ID))
                .willReturn(Optional.of(order));
        given(orderBuyerRepository.findById(ORDER_ID)).willReturn(Optional.of(buyer));
        given(orderFulfillmentRepository.findById(ORDER_ID)).willReturn(Optional.of(fulfillment));
    }

    private CustomerCredentialRequestDto request() {
        return new CustomerCredentialRequestDto(EMAIL, PASSWORD);
    }

    private Order order() {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                "P1-1-09141120-483920",
                customer,
                OrderTestFixtures.project(1L),
                1L,
                OrderStatus.PENDING_DEPOSIT,
                18000,
                0,
                18000,
                now.plusDays(1),
                "김윤진",
                true, now, true, now, true, now
        );
        ReflectionTestUtils.setField(order, "id", ORDER_ID);
        return order;
    }

    private OrderBuyer buyer(String name, String phone) {
        return new OrderBuyer(
                null,
                OrderBuyerType.STUDENT,
                "SEOUL",
                name,
                "이전학과",
                "60200000",
                phone,
                "국민은행",
                "12345601234567",
                "인스타그램",
                EMAIL
        );
    }

    private OrderFulfillment fulfillment() {
        return new OrderFulfillment(
                null,
                OrderFulfillmentMethod.DELIVERY,
                "김윤진",
                "01023456789",
                true,
                "03924",
                "서울 마포구 백범로 35",
                "공학관 512호",
                null
        );
    }
}
