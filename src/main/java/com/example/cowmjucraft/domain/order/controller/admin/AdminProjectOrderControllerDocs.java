package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderBulkAdvanceRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderBulkAdvanceResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminProjectOrderStatisticsResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order - Admin", description = "주문 관리자 API")
public interface AdminProjectOrderControllerDocs {

    @Operation(summary = "프로젝트별 주문 목록 조회", description = "프로젝트 상품이 포함된 주문을 기존 최신순과 상태 필터 기준으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<List<AdminOrderListItemResponseDto>>> getOrders(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId,
            @Parameter(description = "주문 상태 필터") OrderStatus status
    );

    @Operation(summary = "프로젝트 주문 통계 조회", description = "목록 필터와 무관하게 집계 대상 상태의 주문 수와 프로젝트 상품 금액을 조회합니다.")
    ResponseEntity<ApiResult<AdminProjectOrderStatisticsResponseDto>> getStatistics(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId
    );

    @Operation(summary = "개별 주문 상태 다음 단계 진행", description = "정상 주문 흐름의 정확히 다음 단계로만 상태를 변경합니다.")
    ResponseEntity<ApiResult<AdminOrderStatusResponseDto>> advanceOrderStatus(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId,
            @Parameter(description = "주문 ID", example = "10") Long orderId
    );

    @Operation(summary = "주문 상태 일괄 다음 단계 진행", description = "현재 상태가 같은 주문들을 정상 주문 흐름의 정확히 다음 단계로 원자적으로 변경합니다.")
    ResponseEntity<ApiResult<AdminOrderBulkAdvanceResponseDto>> advanceOrderStatuses(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId,
            @Valid AdminOrderBulkAdvanceRequestDto request
    );

}
