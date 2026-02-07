package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회 아이디 사용 가능 여부 응답 DTO")
public record OrderLookupIdAvailabilityResponseDto(
        @Schema(description = "조회 아이디", example = "guest-mju-001")
        String lookupId,

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available
) {
}
