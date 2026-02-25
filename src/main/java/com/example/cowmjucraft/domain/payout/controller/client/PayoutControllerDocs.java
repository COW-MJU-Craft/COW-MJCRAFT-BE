package com.example.cowmjucraft.domain.payout.controller.client;

import com.example.cowmjucraft.domain.payout.dto.response.PayoutDetailResponse;
import com.example.cowmjucraft.domain.payout.dto.response.PayoutListResponse;
import com.example.cowmjucraft.domain.payout.dto.response.PayoutListWrapperResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Payout", description = "정산서 조회 API")
public interface PayoutControllerDocs {

    @Operation(summary = "정산서 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    ResponseEntity<ApiResult<PayoutListWrapperResponse>> getPayoutList();

    @Operation(summary = "정산서 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Payout 없음")
    })
    ResponseEntity<ApiResult<PayoutDetailResponse>> getPayoutDetail(
            @PathVariable Long payoutId
    );
}