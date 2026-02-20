package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderCancelRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Order - Admin", description = "주문 관리자 API")
public interface AdminOrderControllerDocs {

    @Operation(summary = "관리자 주문 목록 조회", description = "status가 있으면 해당 상태만, 없으면 전체 주문을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "400", description = "status 값이 유효하지 않음")
    })
    ResponseEntity<ApiResult<List<AdminOrderListItemResponseDto>>> getOrders(
            @Parameter(description = "주문 상태 필터 (PENDING_DEPOSIT | PAID | CANCELED | REFUND_REQUESTED | REFUNDED)", example = "PENDING_DEPOSIT")
            OrderStatus status
    );

    @Operation(summary = "관리자 주문 상세 조회", description = "주문, 구매자, 수령정보, 주문상품 목록을 조회합니다. 취소 주문은 canceledAt, cancelReason이 함께 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<ApiResult<OrderDetailResponseDto>> getOrderDetail(
            @Parameter(description = "주문 ID", example = "1")
            Long orderId
    );

    @Operation(summary = "관리자 결제 확정", description = "입금 대기(PENDING_DEPOSIT) 주문을 결제 확정(PAID) 처리하고, 조회 토큰을 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "상태 충돌 또는 재고 문제")
    })
    ResponseEntity<ApiResult<AdminOrderStatusResponseDto>> confirmPaid(
            @Parameter(description = "주문 ID", example = "1")
            Long orderId
    );

    @Operation(summary = "관리자 주문 취소/환불요청", description = "PENDING_DEPOSIT은 CANCELED, PAID는 REFUND_REQUESTED로 전이합니다. reason은 선택 입력이며 저장/메일에 사용됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "취소/환불요청 전이가 불가능한 주문 상태")
    })
    ResponseEntity<ApiResult<AdminOrderStatusResponseDto>> cancelOrder(
            @Parameter(description = "주문 ID", example = "1")
            Long orderId,
            @RequestBody(
                    required = false,
                    description = "관리자 주문 취소 요청 (취소 사유는 선택 입력)",
                    content = @Content(
                            schema = @Schema(implementation = AdminOrderCancelRequestDto.class),
                            examples = @ExampleObject(
                                    name = "admin-order-cancel-request",
                                    value = """
                                            {
                                              "reason": "고객 요청"
                                            }
                                            """
                            )
                    )
            )
            AdminOrderCancelRequestDto request
    );

    @Operation(summary = "관리자 환불 완료 확정", description = "REFUND_REQUESTED 상태의 주문만 REFUNDED로 전이하고 조회 토큰을 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "환불 요청 상태가 아닌 주문")
    })
    ResponseEntity<ApiResult<AdminOrderStatusResponseDto>> confirmRefund(
            @Parameter(description = "주문 ID", example = "1")
            Long orderId
    );
}
