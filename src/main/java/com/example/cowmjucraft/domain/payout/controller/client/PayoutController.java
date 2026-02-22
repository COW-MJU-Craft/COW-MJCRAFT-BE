package com.example.cowmjucraft.domain.payout.controller.client;

import com.example.cowmjucraft.domain.payout.dto.response.PayoutDetailResponse;
import com.example.cowmjucraft.domain.payout.dto.response.PayoutListResponse;
import com.example.cowmjucraft.domain.payout.entity.PayoutItem;
import com.example.cowmjucraft.domain.payout.service.client.PayoutService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public ResponseEntity<ApiResult<List<PayoutListResponse>>> getPayoutList() {
        return ApiResponse.of(SuccessType.SUCCESS, payoutService.getPayoutList());
    }

    @GetMapping("{payoutId}")
    public ResponseEntity<ApiResult<PayoutDetailResponse>> getPayoutDetail(@PathVariable Long payoutId) {
        return ApiResponse.of(SuccessType.SUCCESS, payoutService.getPayoutDetail(payoutId));
    }
}
