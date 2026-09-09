package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.service.CustomerAccountService;
import com.example.cowmjucraft.domain.order.OrderTestFixtures;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
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
class AdminOrderBuyerEmailServiceTest {

    private static final Long ORDER_ID = 11L;
    private static final String WRONG_EMAIL = "yunjin@mju.ac.kt";
    private static final String CORRECTED_EMAIL = "yunjin@mju.ac.kr";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderBuyerRepository orderBuyerRepository;

    @Mock
    private CustomerAccountService customerAccountService;

    @Mock
    private OrderViewTokenService orderViewTokenService;

    @Mock
    private MailOutboxService mailOutboxService;

    private AdminOrderBuyerEmailService adminOrderBuyerEmailService;

    @BeforeEach
    void setUp() {
        adminOrderBuyerEmailService = new AdminOrderBuyerEmailService(
                orderRepository,
                orderBuyerRepository,
                customerAccountService,
                orderViewTokenService,
                mailOutboxService
        );
    }

    @Test
    void correctBuyerEmail_스냅샷과고객을함께옮기고메일을재발송한다() {
        // given
        Customer wrongCustomer = new Customer(WRONG_EMAIL);
        Customer correctedCustomer = new Customer(CORRECTED_EMAIL);
        Order order = order(wrongCustomer);
        OrderBuyer buyer = buyer();

        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
        given(orderBuyerRepository.findById(ORDER_ID)).willReturn(Optional.of(buyer));
        given(customerAccountService.upsertForOrder(eq(CORRECTED_EMAIL), any())).willReturn(correctedCustomer);
        given(orderViewTokenService.rotateToken(eq(order), any())).willReturn("raw-token");
        given(orderViewTokenService.buildOrderViewUrl("raw-token")).willReturn("https://example.com/view?token=raw-token");

        // when
        adminOrderBuyerEmailService.correctBuyerEmail(ORDER_ID, "  YunJin@MJU.ac.KR  ");

        // then — 주문 스냅샷·소속 고객·조회 링크 셋을 함께 맞춘다
        assertThat(buyer.getEmail()).isEqualTo(CORRECTED_EMAIL);
        assertThat(order.getCustomer()).isSameAs(correctedCustomer);
        then(orderViewTokenService).should().rotateToken(eq(order), any());
        then(mailOutboxService).should().enqueueOrderViewLink(
                eq(ORDER_ID),
                eq(CORRECTED_EMAIL),
                eq("김윤진"),
                eq(order.getOrderNo()),
                eq("https://example.com/view?token=raw-token"),
                any()
        );
    }

    @Test
    void correctBuyerEmail_주문없음_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminOrderBuyerEmailService.correctBuyerEmail(ORDER_ID, CORRECTED_EMAIL))
                .isInstanceOf(OrderException.class)
                .extracting(exception -> ((OrderException) exception).getErrorCode())
                .isEqualTo(OrderErrorType.ORDER_NOT_FOUND);
    }

    @Test
    void correctBuyerEmail_주문자정보없음_BUYER_NOT_FOUND() {
        // given
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order(new Customer(WRONG_EMAIL))));
        given(orderBuyerRepository.findById(ORDER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminOrderBuyerEmailService.correctBuyerEmail(ORDER_ID, CORRECTED_EMAIL))
                .isInstanceOf(OrderException.class)
                .extracting(exception -> ((OrderException) exception).getErrorCode())
                .isEqualTo(OrderErrorType.BUYER_NOT_FOUND);
    }

    private Order order(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                "P1-1-0914-483920",
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

    private OrderBuyer buyer() {
        return new OrderBuyer(
                null,
                OrderBuyerType.STUDENT,
                "SEOUL",
                "김윤진",
                "융합소프트웨어학부",
                "60201234",
                "010-2345-6789",
                "국민은행",
                "12345601234567",
                null,
                WRONG_EMAIL
        );
    }
}
