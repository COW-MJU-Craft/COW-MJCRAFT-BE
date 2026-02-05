package com.example.cowmjucraft.domain.cart.controller;

import com.example.cowmjucraft.domain.cart.dto.request.CartItemCreateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.request.CartItemQuantityUpdateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.response.CartSummaryResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Cart", description = "장바구니 API")
public interface CartControllerDocs {

    @Operation(summary = "장바구니 조회", description = "현재 로그인한 사용자의 장바구니를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<CartSummaryResponseDto> getCart(
            @Parameter(hidden = true)
            String memberId
    );

    @Operation(summary = "장바구니 아이템 추가", description = "물품을 장바구니에 추가합니다. 기존 아이템이면 수량이 누적됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "물품 없음")
    })
    ApiResult<CartSummaryResponseDto> addCartItem(
            @Parameter(hidden = true)
            String memberId,
            @Valid @RequestBody CartItemCreateRequestDto request
    );

    @Operation(summary = "장바구니 아이템 수량 수정", description = "장바구니 아이템의 수량을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 아이템 없음")
    })
    ApiResult<CartSummaryResponseDto> updateCartItemQuantity(
            @Parameter(hidden = true)
            String memberId,
            @Parameter(description = "장바구니 아이템 ID", example = "10")
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto request
    );

    @Operation(summary = "장바구니 아이템 삭제", description = "선택한 장바구니 아이템을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "장바구니 아이템 없음")
    })
    ApiResult<?> removeCartItem(
            @Parameter(hidden = true)
            String memberId,
            @Parameter(description = "장바구니 아이템 ID", example = "10")
            @PathVariable Long cartItemId
    );

    @Operation(summary = "장바구니 비우기", description = "현재 로그인한 사용자의 장바구니를 비웁니다.")
    @ApiResponse(responseCode = "204", description = "비우기 성공")
    ApiResult<?> clearCart(
            @Parameter(hidden = true)
            String memberId
    );
}
