package com.example.cowmjucraft.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import org.junit.jupiter.api.Test;

class DomainExceptionMessageTest {

    @Test
    void getMessage_detail만_지정_errorCode기본메시지반환() {
        // given
        OrderException exception = new OrderException(OrderErrorType.INSUFFICIENT_STOCK, "projectItemId=42");

        // when
        String message = exception.getMessage();

        // then
        assertThat(message).isEqualTo(OrderErrorType.INSUFFICIENT_STOCK.getMessage());
        assertThat(message).doesNotContain("projectItemId");
        assertThat(exception.getDetail()).isEqualTo("projectItemId=42");
    }

    @Test
    void getMessage_detail미지정_errorCode기본메시지반환() {
        // given
        OrderException exception = new OrderException(OrderErrorType.ORDER_NOT_FOUND);

        // when & then
        assertThat(exception.getMessage()).isEqualTo(OrderErrorType.ORDER_NOT_FOUND.getMessage());
        assertThat(exception.getDetail()).isNull();
    }

    @Test
    void requiredField_필드명포함사용자메시지반환하고_detail은분리() {
        // given
        OrderException exception =
                OrderException.requiredField(OrderErrorType.REQUIRED_FIELD_MISSING, "입금자명");

        // when & then
        assertThat(exception.getMessage()).isEqualTo("입금자명은(는) 필수입니다.");
        assertThat(exception.getDetail()).isEqualTo("field=입금자명");
        assertThat(exception.getErrorCode()).isEqualTo(OrderErrorType.REQUIRED_FIELD_MISSING);
    }
}
