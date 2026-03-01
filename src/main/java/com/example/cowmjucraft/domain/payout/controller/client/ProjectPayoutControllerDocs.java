package com.example.cowmjucraft.domain.payout.controller.client;

import com.example.cowmjucraft.domain.payout.dto.response.PayoutDetailResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface ProjectPayoutControllerDocs {

    @Operation(
            summary = "프로젝트별 정산서 상세 조회",
            description = "마감된 프로젝트에 한하여 해당 프로젝트의 정산서를 상세 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정산서 조회 성공"),
            @ApiResponse(responseCode = "403", description = "마감되지 않은 프로젝트"),
            @ApiResponse(responseCode = "404", description = "프로젝트 또는 정산서를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<PayoutDetailResponse>> getPayoutDetailByProjectId(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );
}
