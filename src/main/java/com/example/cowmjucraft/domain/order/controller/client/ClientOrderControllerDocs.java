package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order - Public", description = "비회원 주문 API")
public interface ClientOrderControllerDocs {

    @Operation(
            summary = "비회원 주문 생성",
            description = """
                    비회원 주문을 생성합니다.
                    - 입금자명(depositorName)은 필수입니다.
                    - 입금 기한(depositDeadline)은 익일 23:59:59로 설정됩니다.
                    - 재고 차감은 주문 생성 시점이 아니라 입금 확인(PAID) 시점에만 수행됩니다.
                    - 응답의 viewToken은 이메일 링크 조회용 원본 토큰이며 DB에는 해시만 저장됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "비회원 주문 생성 요청",
                    content = @Content(
                            schema = @Schema(implementation = OrderCreateRequestDto.class),
                            examples = @ExampleObject(
                                    name = "order-create-request",
                                    value = """
                                            {
                                              "lookupId": "guest-mju-001",
                                              "password": "Pa$$w0rd!",
                                              "depositorName": "홍길동",
                                              "privacyAgreed": true,
                                              "refundAgreed": true,
                                              "cancelRiskAgreed": true,
                                              "items": [
                                                {
                                                  "projectItemId": 1,
                                                  "quantity": 2
                                                }
                                              ],
                                              "buyer": {
                                                "buyerType": "STUDENT",
                                                "campus": "SEOUL",
                                                "name": "홍길동",
                                                "departmentOrMajor": "컴퓨터공학과",
                                                "studentNo": "60123456",
                                                "phone": "010-1234-5678",
                                                "refundBank": "국민은행",
                                                "refundAccount": "123456-78-901234",
                                                "referralSource": "instagram",
                                                "email": "hong@example.com"
                                              },
                                              "fulfillment": {
                                                "method": "DELIVERY",
                                                "receiverName": "홍길동",
                                                "receiverPhone": "010-1234-5678",
                                                "infoConfirmed": true,
                                                "postalCode": "04524",
                                                "addressLine1": "서울시 중구 세종대로 110",
                                                "addressLine2": "101동 1001호",
                                                "deliveryMemo": "부재 시 문 앞에 놓아주세요"
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "주문 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "order-create-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 201,
                                              "message": "성공적으로 생성하였습니다.",
                                              "data": {
                                                "orderId": 1,
                                                "orderNo": "ORD-20260207190000-123456",
                                                "status": "PENDING_DEPOSIT",
                                                "totalAmount": 24000,
                                                "shippingFee": 0,
                                                "finalAmount": 24000,
                                                "depositDeadline": "2026-02-08T23:59:59",
                                                "lookupId": "guest-mju-001",
                                                "viewToken": "raw-view-token-string"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "주문 상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "재고 부족 또는 상태 충돌")
    })
    ApiResult<OrderCreateResponseDto> createOrder(OrderCreateRequestDto request);
}
