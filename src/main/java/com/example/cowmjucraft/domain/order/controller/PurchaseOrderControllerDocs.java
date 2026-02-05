package com.example.cowmjucraft.domain.order.controller;

import com.example.cowmjucraft.domain.order.dto.request.BuyNowRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.CartCheckoutRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.PurchaseOrderCreateResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Order", description = "주문 API")
public interface PurchaseOrderControllerDocs {

    @Operation(summary = "바로 구매", description = "장바구니를 거치지 않고 물품을 바로 주문합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "물품 없음")
    })
    ApiResult<PurchaseOrderCreateResponseDto> buyNow(
            @Parameter(hidden = true)
            String memberId,
            @Valid @RequestBody BuyNowRequestDto request
    );

    @Operation(summary = "장바구니 주문", description = "선택한 장바구니 아이템들로 주문을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 아이템 없음")
    })
    ApiResult<PurchaseOrderCreateResponseDto> checkoutCart(
            @Parameter(hidden = true)
            String memberId,
            @Valid @RequestBody CartCheckoutRequestDto request
    );
}
