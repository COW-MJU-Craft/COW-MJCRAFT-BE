package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "비회원 주문 생성 요청 DTO")
public record OrderCreateRequestDto(
        @Schema(description = "조회 아이디(고유)", example = "guest-mju-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "lookupId는 필수입니다.")
        String lookupId,

        @Schema(description = "조회 비밀번호", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "password는 필수입니다.")
        String password,

        @Schema(description = "입금자명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "depositorName은 필수입니다.")
        String depositorName,

        @Schema(description = "개인정보 수집 및 이용 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "privacyAgreed는 필수입니다.")
        Boolean privacyAgreed,

        @Schema(description = "환불 정책 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "refundAgreed는 필수입니다.")
        Boolean refundAgreed,

        @Schema(description = "주문 취소 리스크 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "cancelRiskAgreed는 필수입니다.")
        Boolean cancelRiskAgreed,

        @ArraySchema(schema = @Schema(implementation = OrderCreateItemRequestDto.class), arraySchema = @Schema(description = "주문 상품 목록", requiredMode = Schema.RequiredMode.REQUIRED))
        @Valid
        @NotEmpty(message = "items는 1개 이상이어야 합니다.")
        List<OrderCreateItemRequestDto> items,

        @Schema(description = "주문자 정보", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "buyer는 필수입니다.")
        OrderCreateBuyerRequestDto buyer,

        @Schema(description = "수령/배송 정보", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "fulfillment는 필수입니다.")
        OrderCreateFulfillmentRequestDto fulfillment
) {
}
