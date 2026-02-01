package com.example.cowmjucraft.domain.recruit.controller.client;

import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationCreateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ResultReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationCreateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationReadResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationUpdateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ResultReadResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Recruit - Application (User)",
        description = "지원서 API"
)
public interface ApplicationControllerDocs {

    @Operation(
            summary = "지원서 생성",
            description = """
                    현재 OPEN 상태인 Form에 대해 지원서를 생성합니다.
                    - 동일 form 내 studentId 중복 지원 불가
                    - 1지망 / 2지망 부서는 서로 달라야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "리소스 없음"),
            @ApiResponse(responseCode = "409", description = "모집 마감 또는 중복 지원")
    })
    ApiResult<ApplicationCreateResponse> createApplication(
            @Parameter(description = "지원서 생성 요청")
            ApplicationCreateRequest request
    );

    // ------------------------------------------------------------

    @Operation(
            summary = "지원서 조회",
            description = """
                    학번(studentId)과 비밀번호로 지원서를 조회합니다.
                    OPEN Form이 존재하면 해당 Form 기준으로,
                    없으면 가장 최근 Form 기준으로 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "지원서 또는 Form을 찾을 수 없음")
    })
    ApiResult<ApplicationReadResponse> readApplication(
            @Parameter(description = "지원서 조회 요청")
            ApplicationReadRequest request
    );

    // ------------------------------------------------------------

    @Operation(
            summary = "지원서 수정",
            description = """
                모집 기간(OPEN) 중에만 지원서를 수정할 수 있습니다.
                - studentId + password 인증이 필요합니다.
                
                답변(answers) 수정 방식
                - 요청에 포함된 answers 항목만 "부분 수정"됩니다.
                - 요청에 포함되지 않은 기존 답변은 그대로 유지됩니다.
                - 동일 formQuestionId에 대한 답변이 이미 존재하면 값이 갱신됩니다.
                - 동일 formQuestionId에 대한 답변이 없으면 새로 생성됩니다.
                - (삭제 정책) value가 null로 전달되면 해당 답변은 삭제됩니다.
                
                부서(firstDepartment/secondDepartment)
                - 부서 변경은 1지망과 2지망이 모두 전달된 경우에만 반영됩니다.
                - 1지망과 2지망은 서로 달라야 합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(부서 중복, 규칙 위반, 폼 불일치 등)"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "리소스 없음(Application/FormQuestion 등)"),
            @ApiResponse(responseCode = "409", description = "모집 마감")
    })
    ApiResult<ApplicationUpdateResponse> updateApplication(
            @Parameter(description = "지원서 수정 요청")
            ApplicationUpdateRequest request
    );

    // ------------------------------------------------------------

    @Operation(
            summary = "지원 결과 조회",
            description = """
                    학번(studentId)과 비밀번호로 지원 결과를 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "지원서 또는 Form을 찾을 수 없음")
    })
    ApiResult<ResultReadResponse> readResult(
            @Parameter(description = "지원 결과 조회 요청")
            ResultReadRequest request
    );
}
