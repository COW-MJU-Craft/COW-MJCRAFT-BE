package com.example.cowmjucraft.domain.introduce.intro.controller.admin;

import com.example.cowmjucraft.domain.introduce.intro.dto.request.IntroduceAdminRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

@Tag(name = "Introduce - Admin", description = "명지공방 소개 관리자 API")
public interface IntroduceAdminControllerDocs {

    @Operation(
            summary = "명지공방 소개 전체 교체",
            description = """
            명지공방 소개 내용을 전체 교체합니다.
            - 요청으로 들어온 내용이 최종 상태가 됩니다.
            - 등록/수정을 PUT 하나로 처리합니다.
            - 이미지는 Media API로 presign 발급 → S3 업로드 → activate 완료 후, 여기에는 mediaId만 연결합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    void replaceIntroduceContent(
            @RequestBody(
                    required = true,
                    description = "교체할 명지공방 소개 정보",
                    content = @Content(schema = @Schema(implementation = IntroduceAdminRequestDto.class))
            )
            @Valid
            IntroduceAdminRequestDto request
    );
}
