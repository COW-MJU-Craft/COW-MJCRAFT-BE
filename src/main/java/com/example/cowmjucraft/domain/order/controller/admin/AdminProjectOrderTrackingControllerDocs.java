package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderTrackingInformationUpdateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderTrackingInformationResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order - Admin", description = "주문 관리자 API")
public interface AdminProjectOrderTrackingControllerDocs {

    @Operation(summary = "주문 운송장 정보 수정", description = "주문 상태와 독립적으로 운송장 정보를 입력, 수정 또는 삭제합니다.")
    ResponseEntity<ApiResult<AdminOrderTrackingInformationResponseDto>> updateTrackingInformation(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId,
            @Parameter(description = "주문 ID", example = "10") Long orderId,
            @Valid AdminOrderTrackingInformationUpdateRequestDto request
    );
}
