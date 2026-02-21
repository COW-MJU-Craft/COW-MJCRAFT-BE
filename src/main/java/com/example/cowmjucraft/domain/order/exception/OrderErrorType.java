package com.example.cowmjucraft.domain.order.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorType implements ErrorCode {

    ORDER_NOT_FOUND(404, "주문을 찾을 수 없습니다."),
    ITEM_NOT_FOUND(404, "상품을 찾을 수 없습니다."),
    BUYER_NOT_FOUND(404, "주문자 정보를 찾을 수 없습니다."),
    FULFILLMENT_NOT_FOUND(404, "수령 정보를 찾을 수 없습니다."),
    INVALID_VIEW_TOKEN(404, "유효하지 않은 조회 링크입니다."),

    EXPIRED_VIEW_TOKEN(410, "만료되었거나 폐기된 조회 링크입니다."),

    INVALID_LOOKUP_CREDENTIALS(401, "조회 아이디 또는 비밀번호가 올바르지 않습니다."),

    DUPLICATED_LOOKUP_ID(409, "이미 사용 중인 조회 아이디입니다."),
    ITEM_NOT_AVAILABLE(409, "판매 중인 상품만 주문할 수 있습니다."),
    INSUFFICIENT_STOCK(409, "재고가 부족하여 주문할 수 없습니다."),
    ALREADY_STOCK_DEDUCTED(409, "이미 재고 차감이 완료된 주문입니다."),
    STOCK_INFO_MISSING(409, "상품 재고 정보가 없어 결제를 진행할 수 없습니다."),
    INVALID_STATUS_TRANSITION(409, "주문 상태 전이가 허용되지 않습니다."),

    VIEW_TOKEN_REQUIRED(400, "조회 토큰이 필요합니다."),
    ORDER_ITEMS_REQUIRED(400, "주문 상품은 1개 이상이어야 합니다."),
    ORDER_ITEMS_EMPTY(400, "주문 항목이 없어 결제 확정을 진행할 수 없습니다."),
    INVALID_ORDER_ITEM(400, "상품 정보가 올바르지 않습니다."),
    QUANTITY_MUST_BE_POSITIVE(400, "주문 수량은 1개 이상이어야 합니다."),
    NORMAL_SALE_ONLY(400, "현재는 NORMAL 판매 상품만 주문할 수 있습니다."),
    NON_NORMAL_SALE_NOT_DEDUCTIBLE(400, "NORMAL 상품만 결제 확정 시 재고 차감이 가능합니다."),
    DELIVERY_ADDRESS_REQUIRED(400, "배송 주문은 우편번호와 기본 주소가 필수입니다."),
    REQUIRED_FIELD_MISSING(400, "필수 입력값이 누락되었습니다."),
    PRIVACY_AGREEMENT_REQUIRED(400, "개인정보 수집 및 이용 동의가 필요합니다."),
    REFUND_AGREEMENT_REQUIRED(400, "환불 정책 동의가 필요합니다."),
    CANCEL_RISK_AGREEMENT_REQUIRED(400, "주문 취소 리스크 동의가 필요합니다."),
    ORDER_AMOUNT_OVERFLOW(400, "주문 금액 계산 중 오류가 발생했습니다."),
    LOOKUP_FIELD_REQUIRED(400, "필수 조회 필드가 누락되었습니다."),

    ORDER_NO_GENERATION_FAILED(500, "주문 번호 생성에 실패했습니다."),
    TOKEN_HASH_FAILED(500, "토큰 처리 중 오류가 발생했습니다."),
    EMAIL_SEND_FAILED(500, "주문 메일 발송에 실패했습니다.");

    private final int httpStatusCode;
    private final String message;
}
