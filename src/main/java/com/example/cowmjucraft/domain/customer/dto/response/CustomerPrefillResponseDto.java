package com.example.cowmjucraft.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 주문서 "저장한 정보 불러오기" 응답.
 *
 * <p>환불 은행/계좌·입금자명·수령지는 {@code customers}가 아니라 <b>최근 주문
 * 스냅샷</b>에서 읽어 채운다. 인증을 통과한 사람은 주문 상세로 이미 볼 수 있는
 * 값이므로 폼에 채우는 것이 새로운 노출이 아니고, 저장 위치를 늘리지 않으면서
 * 입력 부담만 없앤다.
 */
@Schema(description = "고객 정보 불러오기 응답 DTO")
public record CustomerPrefillResponseDto(

        @Schema(description = "값의 출처. PROFILE(직접 저장한 프로필) | LAST_ORDER(최근 주문) | null(없음)", example = "PROFILE")
        String source,

        @Schema(description = "구매자 정보. 채울 값이 전혀 없으면 null")
        BuyerPrefill buyer,

        @Schema(description = "수령 정보. 최근 주문이 없으면 null")
        FulfillmentPrefill fulfillment
) {

    @Schema(description = "구매자 정보 자동완성 값")
    public record BuyerPrefill(
            @Schema(description = "이름", example = "김윤진") String name,
            @Schema(description = "연락처", example = "010-2345-6789") String phone,
            @Schema(description = "이메일", example = "yunjin@mju.ac.kr") String email,
            @Schema(description = "구분", example = "STUDENT") String buyerType,
            @Schema(description = "캠퍼스", example = "SEOUL") String campus,
            @Schema(description = "학과 / 전공", example = "융합소프트웨어학부") String departmentOrMajor,
            @Schema(description = "학번", example = "60201234") String studentNo,
            @Schema(description = "환불 은행. 최근 주문 스냅샷에서 온다", example = "국민은행") String refundBank,
            @Schema(description = "환불 계좌. 최근 주문 스냅샷에서 온다", example = "12345601234567") String refundAccount,
            @Schema(description = "유입 경로", example = "인스타그램") String referralSource,
            @Schema(description = "입금자명. 최근 주문 스냅샷에서 온다", example = "김윤진") String depositorName
    ) {
    }

    @Schema(description = "수령 정보 자동완성 값")
    public record FulfillmentPrefill(
            @Schema(description = "수령 방법", example = "DELIVERY") String method,
            @Schema(description = "수령인", example = "김윤진") String receiverName,
            @Schema(description = "수령인 연락처", example = "010-2345-6789") String receiverPhone,
            @Schema(description = "우편번호", example = "03924") String postalCode,
            @Schema(description = "기본 주소", example = "서울 마포구 백범로 35") String addressLine1,
            @Schema(description = "상세 주소", example = "공학관 512호") String addressLine2
    ) {
    }
}
