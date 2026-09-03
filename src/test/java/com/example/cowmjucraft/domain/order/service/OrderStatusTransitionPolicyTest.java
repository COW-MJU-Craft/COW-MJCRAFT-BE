package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OrderStatusTransitionPolicyTest {

    static Object[][] 정상주문상태전이() {
        return new Object[][]{
                {OrderStatus.PENDING_DEPOSIT, OrderStatus.PAID},
                {OrderStatus.PAID, OrderStatus.IN_PRODUCTION},
                {OrderStatus.IN_PRODUCTION, OrderStatus.READY_TO_SHIP},
                {OrderStatus.READY_TO_SHIP, OrderStatus.DELIVERED}
        };
    }

    @ParameterizedTest
    @MethodSource("정상주문상태전이")
    void nextNormalStatus_정상진행상태_정확히다음상태반환(OrderStatus current, OrderStatus expected) {
        // when
        OrderStatus next = OrderStatusTransitionPolicy.nextNormalStatus(current);

        // then
        assertThat(next).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("정상주문상태전이")
    void validate_정상진행의다음상태_전이허용(OrderStatus current, OrderStatus requested) {
        // when & then
        assertThatCode(() -> OrderStatusTransitionPolicy.validate(current, requested))
                .doesNotThrowAnyException();
    }

    static Object[][] 신규상태의취소환불전이() {
        return new Object[][]{
                {OrderStatus.IN_PRODUCTION, OrderStatus.CANCELED},
                {OrderStatus.IN_PRODUCTION, OrderStatus.REFUND_REQUESTED},
                {OrderStatus.READY_TO_SHIP, OrderStatus.CANCELED},
                {OrderStatus.READY_TO_SHIP, OrderStatus.REFUND_REQUESTED},
                {OrderStatus.DELIVERED, OrderStatus.CANCELED},
                {OrderStatus.DELIVERED, OrderStatus.REFUND_REQUESTED}
        };
    }

    @ParameterizedTest
    @MethodSource("신규상태의취소환불전이")
    void validate_신규진행상태에서취소환불요청_OrderException발생(
            OrderStatus current,
            OrderStatus requested
    ) {
        // when & then
        assertThatThrownBy(() -> OrderStatusTransitionPolicy.validate(current, requested))
                .isInstanceOf(OrderException.class);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELED", "REFUND_REQUESTED", "REFUNDED"})
    void nextNormalStatus_진행불가능상태_OrderException발생(OrderStatus current) {
        // when & then
        assertThatThrownBy(() -> OrderStatusTransitionPolicy.nextNormalStatus(current))
                .isInstanceOf(OrderException.class);
    }
}
