package com.example.cowmjucraft.domain.sns.controller.admin;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Introduce - Admin", description = "SNS 소개 관리자 API")
public interface SnsAdminControllerDocs {

    @Operation(
            summary = "SNS 링크 전체 교체",
            description = """
            SNS 링크를 전체 교체합니다.
            - 요청으로 들어온 리스트가 최종 상태가 됩니다.
            - 등록/수정/삭제를 PUT 하나로 처리합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    void replaceSnsLinks(
            @RequestBody(
                    required = true,
                    description = "전체 교체할 SNS 링크 목록",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = SnsAdminRequestDto.class))
                    )
            )
            @Valid
            List<SnsAdminRequestDto> requests
    );
}