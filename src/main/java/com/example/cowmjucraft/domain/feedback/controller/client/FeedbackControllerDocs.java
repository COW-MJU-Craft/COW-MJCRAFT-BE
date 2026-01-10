package com.example.cowmjucraft.domain.feedback.controller.client;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

@Tag(name = "Feedback (Public)", description = "건의사항 등록 API")
public interface FeedbackControllerDocs {

    @Operation(
            summary = "건의사항 등록",
            description = "사용자가 건의사항을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패")
    })
    void createFeedback(
            @RequestBody(
                    required = true,
                    description = "건의사항 등록 정보",
                    content = @Content(schema = @Schema(implementation = FeedbackRequestDto.class))
            )
            @Valid
            FeedbackRequestDto request
    );
}
