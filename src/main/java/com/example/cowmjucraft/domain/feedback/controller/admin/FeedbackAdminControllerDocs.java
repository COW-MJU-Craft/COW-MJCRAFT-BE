package com.example.cowmjucraft.domain.feedback.controller.admin;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Feedback (Admin)", description = "건의사항 관리자 API")
public interface FeedbackAdminControllerDocs {

    @Operation(
            summary = "건의사항 목록 조회",
            description = "등록된 건의사항 목록을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FeedbackAdminResponseDto.class)))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    List<FeedbackAdminResponseDto> getFeedbacks();

    @Operation(
            summary = "건의사항 답변 및 상태 변경",
            description = "관리자가 답변과 처리 상태를 업데이트합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "대상 없음")
    })
    void updateFeedback(
            @Parameter(
                    description = "피드백 ID",
                    example = "1"
            )
            Long id,
            @RequestBody(
                    required = true,
                    description = "답변 및 상태 변경 정보",
                    content = @Content(schema = @Schema(implementation = FeedbackAdminUpdateRequestDto.class))
            )
            @Valid
            FeedbackAdminUpdateRequestDto request
    );
}
