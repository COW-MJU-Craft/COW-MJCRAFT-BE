package com.example.cowmjucraft.domain.cart.controller;

import com.example.cowmjucraft.domain.cart.dto.request.CartItemCreateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.request.CartItemQuantityUpdateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.response.CartSummaryResponseDto;
import com.example.cowmjucraft.domain.cart.service.CartService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage/cart")
public class CartController implements CartControllerDocs {

    private final CartService cartService;

    @GetMapping
    @Override
    public ApiResult<CartSummaryResponseDto> getCart(@AuthenticationPrincipal String memberId) {
        return ApiResult.success(SuccessType.SUCCESS, cartService.getCart(parseMemberId(memberId)));
    }

    @PostMapping
    @Override
    public ApiResult<CartSummaryResponseDto> addCartItem(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody CartItemCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, cartService.addCartItem(parseMemberId(memberId), request));
    }

    @PutMapping("/{cartItemId}")
    @Override
    public ApiResult<CartSummaryResponseDto> updateCartItemQuantity(
            @AuthenticationPrincipal String memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto request
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                cartService.updateCartItemQuantity(parseMemberId(memberId), cartItemId, request)
        );
    }

    @DeleteMapping("/{cartItemId}")
    @Override
    public ApiResult<?> removeCartItem(
            @AuthenticationPrincipal String memberId,
            @PathVariable Long cartItemId
    ) {
        cartService.removeCartItem(parseMemberId(memberId), cartItemId);
        return ApiResult.success(SuccessType.NO_CONTENT);
    }

    @DeleteMapping
    @Override
    public ApiResult<?> clearCart(@AuthenticationPrincipal String memberId) {
        cartService.clearCart(parseMemberId(memberId));
        return ApiResult.success(SuccessType.NO_CONTENT);
    }

    private Long parseMemberId(String principal) {
        try {
            return Long.valueOf(principal);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid member id");
        }
    }
}
