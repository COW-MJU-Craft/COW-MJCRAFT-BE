package com.example.cowmjucraft.domain.payout.controller.admin;

import com.example.cowmjucraft.domain.payout.dto.request.PayoutCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.service.admin.PayoutAdminService;
import com.example.cowmjucraft.domain.payout.service.admin.PayoutItemAdminService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/admin/payouts")
@RestController
@AllArgsConstructor
@RequiredArgsConstructor
public class PayoutAdminController implements PayoutAdminControllerDocs{

    private final PayoutAdminService payoutAdminService;
    private final PayoutItemAdminService payoutItemAdminService;

    @PostMapping
    public ResponseEntity<ApiResult<Map<String, Long>>> createPayout(@RequestBody PayoutCreateAdminRequest payoutCreateAdminRequest) {
        Long payoutId = payoutAdminService.createPayout(payoutCreateAdminRequest);

        return ApiResponse.of(SuccessType.CREATED, Map.of("payoutId", payoutId));
    }

    @PutMapping("/{payoutId}")
    public ResponseEntity<ApiResult<Void>> updatePayout(@PathVariable Long payoutId, @RequestBody PayoutUpdateAdminRequest payoutUpdateAdminRequest) {
        payoutAdminService.updatePayout(payoutId, payoutUpdateAdminRequest);

        return ApiResponse.of(SuccessType.SUCCESS, null);
    }

    @DeleteMapping("/{payoutId}")
    public ResponseEntity<ApiResult<Void>> deletePayout(@PathVariable Long payoutId) {
        payoutAdminService.deletePayout(payoutId);

        return ApiResponse.of(SuccessType.SUCCESS, null);
    }

    @PostMapping("/{payoutId}/items")
    public ResponseEntity<ApiResult<Map<String, Long>>> createPayoutItem(@PathVariable Long payoutId, @RequestBody PayoutItemCreateAdminRequest payoutItemCreateAdminRequest) {
        Long payoutItemId = payoutItemAdminService.createPayoutItem(payoutId, payoutItemCreateAdminRequest);

        return ApiResponse.of(SuccessType.CREATED, Map.of("payoutItemId", payoutItemId));
    }

    @PutMapping("/{payoutId}/items/{payoutItemId}")
    public ResponseEntity<ApiResult<Void>> updatePayoutItem(@PathVariable Long payoutId, @PathVariable Long payoutItemId, @RequestBody PayoutItemUpdateAdminRequest payoutItemUpdateAdminRequest) {
        payoutItemAdminService.updatePayoutItem(payoutId,payoutItemId,payoutItemUpdateAdminRequest);

        return ApiResponse.of(SuccessType.SUCCESS, null);
    }

    @DeleteMapping("/{payoutId}/items/{payoutItemId}")
    public ResponseEntity<ApiResult<Void>> deletePayoutItem(@PathVariable Long payoutId, @PathVariable Long payoutItemId) {
        payoutItemAdminService.deletePayoutItem(payoutId, payoutItemId);

        return ApiResponse.of(SuccessType.SUCCESS, null);
    }
}
