package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order - Admin", description = "주문 관리자 API")
public interface AdminOrderExportControllerDocs {

    @Operation(
            summary = "프로젝트별 주문 CSV 다운로드",
            description = "선택한 프로젝트의 주문 상세 정보를 CSV로 다운로드합니다. 날짜는 둘 다 생략하거나 둘 다 입력해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV 다운로드 성공",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
            ),
            @ApiResponse(responseCode = "400", description = "날짜 범위가 올바르지 않음"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    ResponseEntity<byte[]> exportProjectOrders(
            @Parameter(description = "프로젝트 ID", example = "1") Long projectId,
            @Parameter(description = "조회 시작일(포함)", example = "2026-09-01") LocalDate startDate,
            @Parameter(description = "조회 종료일(포함)", example = "2026-09-05") LocalDate endDate,
            @Parameter(description = "주문 상태") OrderStatus status,
            @Parameter(description = "수령 방식") OrderFulfillmentMethod fulfillmentMethod
    );

    @Operation(
            summary = "날짜별 주문 CSV 다운로드",
            description = "모든 프로젝트에서 지정한 날짜 범위에 생성된 주문 상세 정보를 CSV로 다운로드합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV 다운로드 성공",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
            ),
            @ApiResponse(responseCode = "400", description = "날짜 범위가 올바르지 않음")
    })
    ResponseEntity<byte[]> exportOrdersByDate(
            @Parameter(description = "조회 시작일(포함)", example = "2026-09-01", required = true) LocalDate startDate,
            @Parameter(description = "조회 종료일(포함)", example = "2026-09-05", required = true) LocalDate endDate,
            @Parameter(description = "주문 상태") OrderStatus status,
            @Parameter(description = "수령 방식") OrderFulfillmentMethod fulfillmentMethod
    );
}
