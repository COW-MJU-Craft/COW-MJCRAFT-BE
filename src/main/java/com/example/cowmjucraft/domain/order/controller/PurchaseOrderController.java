package com.example.cowmjucraft.domain.order.controller;

import com.example.cowmjucraft.domain.order.dto.request.BuyNowRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.CartCheckoutRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.PurchaseOrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.service.PurchaseOrderService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage/orders")
public class PurchaseOrderController implements PurchaseOrderControllerDocs {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/buy-now")
    @Override
    public ApiResult<PurchaseOrderCreateResponseDto> buyNow(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody BuyNowRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, purchaseOrderService.buyNow(parseMemberId(memberId), request));
    }

    @PostMapping("/checkout-cart")
    @Override
    public ApiResult<PurchaseOrderCreateResponseDto> checkoutCart(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody CartCheckoutRequestDto request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                purchaseOrderService.checkoutCart(parseMemberId(memberId), request)
        );
    }

    private Long parseMemberId(String principal) {
        try {
            return Long.valueOf(principal);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid member id");
        }
    }
}
