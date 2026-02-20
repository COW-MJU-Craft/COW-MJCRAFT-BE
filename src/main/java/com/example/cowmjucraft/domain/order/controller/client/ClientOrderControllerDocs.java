package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderLookupRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderLookupIdAvailabilityResponseDto;
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
            requestBody = @RequestBody(
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
    ResponseEntity<ApiResult<OrderCreateResponseDto>> createOrder(OrderCreateRequestDto request);

    @Operation(
            summary = "조회 아이디 사용 가능 여부 확인",
            description = "주문 생성 전에 조회 아이디의 중복 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "확인 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "lookup-id-availability-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "lookupId": "guest-mju-001",
                                                "available": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "lookupId 누락 또는 공백")
    })
    ResponseEntity<ApiResult<OrderLookupIdAvailabilityResponseDto>> checkLookupIdAvailability(String lookupId);

    @Operation(
            summary = "조회 아이디/비밀번호로 주문 상세 조회",
            description = """
                    비회원 주문을 조회 아이디와 비밀번호로 조회합니다.
                    - 조회 아이디 또는 비밀번호가 일치하지 않으면 401 Unauthorized를 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "조회 아이디 또는 비밀번호 불일치")
    })
    ResponseEntity<ApiResult<OrderDetailResponseDto>> lookupOrder(OrderLookupRequestDto request);

    @Operation(
            summary = "이메일 토큰으로 주문 상세 조회",
            description = """
                    이메일로 받은 조회 링크의 토큰으로 비회원 주문 상세를 조회합니다.
                    - 토큰이 없거나 공백이면 400 Bad Request
                    - 토큰이 유효하지 않으면 404 Not Found
                    - 토큰이 만료되면 410 Gone
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "order-view-by-token-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "order": {
                                                  "orderNo": "ORD-20260207190000-123456",
                                                  "status": "PENDING_DEPOSIT",
                                                  "totalAmount": 24000,
                                                  "shippingFee": 0,
                                                  "finalAmount": 24000,
                                                  "depositDeadline": "2026-02-08T23:59:59",
                                                  "createdAt": "2026-02-07T19:00:00"
                                                },
                                                "buyer": {
                                                  "name": "홍길동",
                                                  "phone": "010-1234-5678",
                                                  "email": "hong@example.com",
                                                  "buyerType": "STUDENT",
                                                  "campus": "SEOUL",
                                                  "departmentOrMajor": "컴퓨터공학과",
                                                  "studentNo": "60123456",
                                                  "refundBank": "국민은행",
                                                  "refundAccount": "123456-78-901234",
                                                  "referralSource": "instagram"
                                                },
                                                "fulfillment": {
                                                  "method": "DELIVERY",
                                                  "receiverName": "홍길동",
                                                  "receiverPhone": "010-1234-5678",
                                                  "addressLine1": "서울시 중구 세종대로 110",
                                                  "addressLine2": "101동 1001호",
                                                  "postalCode": "04524",
                                                  "deliveryMemo": "부재 시 문 앞에 놓아주세요"
                                                },
                                                "items": [
                                                  {
                                                    "projectItemId": 1,
                                                    "itemNameSnapshot": "후드티",
                                                    "quantity": 2,
                                                    "unitPrice": 12000,
                                                    "lineAmount": 24000
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "token 누락 또는 공백"),
            @ApiResponse(responseCode = "404", description = "토큰 없음 또는 불일치"),
            @ApiResponse(responseCode = "410", description = "토큰 만료")
    })
    ResponseEntity<ApiResult<OrderDetailResponseDto>> viewOrderByToken(
            @Parameter(description = "이메일 링크 조회 토큰", required = true, example = "raw-view-token-string")
            String token
    );
}
