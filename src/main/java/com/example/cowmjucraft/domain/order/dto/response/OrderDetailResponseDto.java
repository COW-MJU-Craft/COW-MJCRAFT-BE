package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "비회원 주문 상세 응답 DTO")
public record OrderDetailResponseDto(
        @Schema(description = "주문 정보")
        OrderInfo order,

        @Schema(description = "구매자 정보")
        BuyerInfo buyer,

        @Schema(description = "수령/배송 정보")
        FulfillmentInfo fulfillment,

        @ArraySchema(arraySchema = @Schema(description = "주문 상품 목록"))
        List<ItemInfo> items
) {

    public record OrderInfo(
            @Schema(description = "주문 번호", example = "ORD-20260207190000-123456")
            String orderNo,

            @Schema(description = "주문 상태", example = "PENDING_DEPOSIT")
            String status,

            @Schema(description = "상품 합계 금액", example = "24000")
            int totalAmount,

            @Schema(description = "배송비", example = "0")
            int shippingFee,

            @Schema(description = "최종 결제 금액", example = "24000")
            int finalAmount,

            @Schema(description = "입금 기한", example = "2026-02-08T23:59:59")
            LocalDateTime depositDeadline,

            @Schema(description = "주문 생성 시각", example = "2026-02-07T19:00:00")
            LocalDateTime createdAt
    ) {
    }

    public record BuyerInfo(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "연락처", example = "010-1234-5678")
            String phone,

            @Schema(description = "이메일", example = "hong@example.com")
            String email,

            @Schema(description = "구매자 유형", example = "STUDENT")
            String buyerType,

            @Schema(description = "캠퍼스", example = "SEOUL")
            String campus,

            @Schema(description = "학과/전공", example = "컴퓨터공학과")
            String departmentOrMajor,

            @Schema(description = "학번", example = "60123456")
            String studentNo,

            @Schema(description = "환불 은행", example = "국민은행")
            String refundBank,

            @Schema(description = "환불 계좌", example = "123456-78-901234")
            String refundAccount,

            @Schema(description = "유입 경로", example = "instagram")
            String referralSource
    ) {
    }

    public record FulfillmentInfo(
            @Schema(description = "수령 방법", example = "DELIVERY")
            String method,

            @Schema(description = "수령인 이름", example = "홍길동")
            String receiverName,

            @Schema(description = "수령인 연락처", example = "010-1234-5678")
            String receiverPhone,

            @Schema(description = "기본 주소", example = "서울시 중구 세종대로 110")
            String addressLine1,

            @Schema(description = "상세 주소", example = "101동 1001호")
            String addressLine2,

            @Schema(description = "우편번호", example = "04524")
            String postalCode,

            @Schema(description = "배송 메모", example = "부재 시 문 앞에 놓아주세요")
            String deliveryMemo
    ) {
    }

    public record ItemInfo(
            @Schema(description = "프로젝트 상품 ID", example = "1")
            Long projectItemId,

            @Schema(description = "상품명 스냅샷", example = "후드티")
            String itemNameSnapshot,

            @Schema(description = "수량", example = "2")
            int quantity,

            @Schema(description = "단가", example = "12000")
            int unitPrice,

            @Schema(description = "라인 금액", example = "24000")
            int lineAmount
    ) {
    }
}
