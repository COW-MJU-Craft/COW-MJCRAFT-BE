package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Recruit - Application (Admin)", description = "지원서 관리자 API")
public interface ApplicationAdminControllerDocs {

    @Operation(summary = "지원서 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<List<ApplicationListAdminResponse>> getApplicationsByFormId(@PathVariable Long formId);

    @Operation(summary = "지원서 상세 조회")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200"),
            @ApiResponse(description = "Form 또는 Application 없음", responseCode = "404"),
            @ApiResponse(description = "해당 Form의 Application이 아님", responseCode = "400")
    })
    ApiResult<ApplicationDetailAdminResponse> getApplicationByFormId(
            @PathVariable Long formId,
            @PathVariable Long applicationId
    );

    @Operation(summary = "지원서 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Application 없음")
    })
    ApiResult<?> deleteApplication(@PathVariable Long applicationId);

    @Operation(summary = "지원 결과 입력")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Application 없음")
    })
    ApiResult<?> updateResult(
            @PathVariable Long applicationId,
            @RequestBody ApplicationResultUpdateAdminRequest request
    );
}