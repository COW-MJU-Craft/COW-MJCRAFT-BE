package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.AddQuestionAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCopyAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCreateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormQuestionUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.*;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(
        name = "Recruit - Form (Admin)",
        description = "Form 관리자 API"
)
public interface FormAdminControllerDocs {

    @Operation(
            summary = "Form 생성",
            description = "모집 Form을 생성합니다. open=true로 생성하면 기존 open Form이 있으면 close 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ApiResult<FormCreateAdminResponse> createForm(
            @Parameter(description = "Form 생성 요청")
            FormCreateAdminRequest request
    );

    // ------------------------------------------------------------

    @Operation(summary = "Form OPEN", description = "지정한 Form을 OPEN 상태로 변경합니다. 다른 OPEN Form이 있으면 close 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<?> openForm(@Parameter(description = "Form ID", example = "1") Long formId);

    @Operation(summary = "Form CLOSE", description = "지정한 Form을 CLOSE 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<?> closeForm(@Parameter(description = "Form ID", example = "1") Long formId);

    // ------------------------------------------------------------

    @Operation(
            summary = "문항 추가",
            description = """
                    Form에 문항(FormQuestion)을 추가합니다.
                    - sectionType=COMMON이면 departmentType은 null이어야 합니다.
                    - answerType!=SELECT이면 selectOptions는 null이어야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "규칙 위반/잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<AddQuestionAdminResponse> addQuestion(
            @Parameter(description = "Form ID", example = "1") Long formId,
            @Parameter(description = "문항 추가 요청") AddQuestionAdminRequest request
    );

    // ------------------------------------------------------------

    @Operation(summary = "Form 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    ApiResult<List<FormListAdminResponse>> getForms();

    @Operation(summary = "Form 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<FormDetailAdminResponse> getForm(
            @Parameter(description = "Form ID", example = "1") Long formId
    );

    // ------------------------------------------------------------

    @Operation(summary = "Form 문항 목록 조회", description = "Form에 속한 문항(FormQuestion)을 questionOrder 오름차순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "Form 없음")
    })
    ApiResult<List<FormQuestionListAdminResponse>> getFormQuestions(
            @Parameter(description = "Form ID", example = "1") Long formId
    );

    // ------------------------------------------------------------

    @Operation(summary = "Form 문항 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "Form 불일치"),
            @ApiResponse(responseCode = "404", description = "FormQuestion 없음")
    })
    ApiResult<?> deleteFormQuestion(
            @Parameter(description = "Form ID", example = "1") Long formId,
            @Parameter(description = "FormQuestion ID", example = "10") Long formQuestionId
    );

    @Operation(summary = "Form 문항 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "규칙 위반/Form 불일치"),
            @ApiResponse(responseCode = "404", description = "FormQuestion 없음")
    })
    ApiResult<?> updateFormQuestion(
            @Parameter(description = "Form ID", example = "1") Long formId,
            @Parameter(description = "FormQuestion ID", example = "10") Long formQuestionId,
            @Parameter(description = "문항 수정 요청") FormQuestionUpdateAdminRequest request
    );

    // ------------------------------------------------------------

    @Operation(summary = "문항 복사(덮어쓰기)", description = "sourceFormId의 문항을 targetFormId로 복사하며, target의 기존 문항은 전부 삭제 후 복사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "요청 규칙 위반(같은 form 복사 등)"),
            @ApiResponse(responseCode = "404", description = "source/target Form 없음")
    })
    ApiResult<FormCopyAdminResponse> copyFormQuestionsOverwrite(
            @Parameter(description = "대상 Form ID", example = "2") Long targetFormId,
            @Parameter(description = "복사 요청") FormCopyAdminRequest request
    );
}
