package com.example.cowmjucraft.domain.payout.controller.admin;

import com.example.cowmjucraft.domain.payout.dto.request.PayoutCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.service.admin.PayoutAdminService;
import com.example.cowmjucraft.domain.payout.service.admin.PayoutItemAdminService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("api/admin/payouts")
@RestController
@AllArgsConstructor
public class PayoutAdminController {

    PayoutAdminService payoutAdminService;
    PayoutItemAdminService payoutItemAdminService;

    @PostMapping
    public ApiResult<Map<String, Long>> createPayout(@RequestBody PayoutCreateAdminRequest payoutCreateAdminRequest) {
        Long payoutId = payoutAdminService.createPayout(payoutCreateAdminRequest);

        return ApiResult.success(SuccessType.SUCCESS, Map.of("payoutId", payoutId));
    }

    @PutMapping("/payoutId")
    public ApiResult<Void> updatePayout(@PathVariable Long payoutId, @RequestBody PayoutUpdateAdminRequest payoutUpdateAdminRequest) {
        payoutAdminService.updatePayout(payoutId, payoutUpdateAdminRequest);

        return ApiResult.success(SuccessType.SUCCESS, null);
    }

    @DeleteMapping("/{payoutId}")
    public ApiResult<Void> deletePayout(@PathVariable Long payoutId) {
        payoutAdminService.deletePayout(payoutId);

        return ApiResult.success(SuccessType.SUCCESS, null);
    }

    @PostMapping("/{payoutId}/items")
    public ApiResult<Map<String, Long>> createPayoutItem(@PathVariable Long payoutId, @RequestBody PayoutItemCreateAdminRequest payoutItemCreateAdminRequest) {
        Long payoutItemId = payoutItemAdminService.createPayoutItem(payoutId, payoutItemCreateAdminRequest);

        return ApiResult.success(SuccessType.SUCCESS, Map.of("payoutItemId", payoutItemId));
    }

    @PutMapping("/{payoutId}/items/{payoutItemId}")
    public ApiResult<Void> updatePayoutItem(@PathVariable Long payoutId, @PathVariable Long payoutItemId, @RequestBody PayoutItemUpdateAdminRequest payoutItemUpdateAdminRequest) {
        payoutItemAdminService.updatePayoutItem(payoutId,payoutItemId,payoutItemUpdateAdminRequest);

        return ApiResult.success(SuccessType.SUCCESS, null);
    }

    @DeleteMapping("/{payoutId}/items/{payoutItemId}")
    public ApiResult<Void> deletePayoutItem(@PathVariable Long payoutId, Long payoutItemId) {
        payoutItemAdminService.deletePayoutItem(payoutId, payoutItemId);

        return ApiResult.success(SuccessType.SUCCESS, null);
    }
}
