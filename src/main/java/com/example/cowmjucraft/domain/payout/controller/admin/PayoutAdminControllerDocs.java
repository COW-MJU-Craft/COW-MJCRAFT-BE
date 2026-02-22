package com.example.cowmjucraft.domain.payout.controller.admin;

import com.example.cowmjucraft.domain.payout.dto.request.PayoutCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutUpdateAdminRequest;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Payout (Admin)", description = "정산서 관리자 API")
public interface PayoutAdminControllerDocs {

    @Operation(summary = "정산서 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    ResponseEntity<ApiResult<Map<String, Long>>> createPayout(
            @RequestBody PayoutCreateAdminRequest request
    );

    @Operation(summary = "정산서 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Payout 없음")
    })
    ResponseEntity<ApiResult<Void>> updatePayout(
            @PathVariable Long payoutId,
            @RequestBody PayoutUpdateAdminRequest request
    );

    @Operation(summary = "정산서 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Payout 없음")
    })
    ResponseEntity<ApiResult<Void>> deletePayout(
            @PathVariable Long payoutId
    );

    @Operation(summary = "정산 항목 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Payout 없음")
    })
    ResponseEntity<ApiResult<Map<String, Long>>> createPayoutItem(
            @PathVariable Long payoutId,
            @RequestBody PayoutItemCreateAdminRequest request
    );

    @Operation(summary = "정산 항목 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "PayoutItem 없음"),
            @ApiResponse(responseCode = "400", description = "해당 Payout의 항목이 아님")
    })
    ResponseEntity<ApiResult<Void>> updatePayoutItem(
            @PathVariable Long payoutId,
            @PathVariable Long payoutItemId,
            @RequestBody PayoutItemUpdateAdminRequest request
    );

    @Operation(summary = "정산 항목 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "PayoutItem 없음"),
            @ApiResponse(responseCode = "400", description = "해당 Payout의 항목이 아님")
    })
    ResponseEntity<ApiResult<Void>> deletePayoutItem(
            @PathVariable Long payoutId,
            @PathVariable Long payoutItemId
    );
}