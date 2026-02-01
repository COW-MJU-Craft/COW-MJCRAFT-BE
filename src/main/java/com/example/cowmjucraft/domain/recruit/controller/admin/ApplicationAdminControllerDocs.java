package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.*;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@Tag(name = "Recruit - Application (Admin)", description = "지원서 관리자 API")
public interface ApplicationAdminControllerDocs {

    @Operation(summary = "지원서 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<List<ApplicationListAdminResponse>> list(Long formId);

    @Operation(summary = "지원서 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 또는 Application 없음"),
            @ApiResponse(responseCode = "400", description = "해당 Form의 Application이 아님")
    })
    ApiResult<ApplicationDetailAdminResponse> detail(Long formId, Long applicationId);

    @Operation(summary = "지원서 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Application 없음")
    })
    ApiResult<?> delete(Long applicationId);

    @Operation(summary = "지원 결과 입력")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Application 없음")
    })
    ApiResult<?> updateResult(Long applicationId, ApplicationResultUpdateAdminRequest request);
}
