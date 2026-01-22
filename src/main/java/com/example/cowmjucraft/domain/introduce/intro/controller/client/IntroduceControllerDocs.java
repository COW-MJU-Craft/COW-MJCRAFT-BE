package com.example.cowmjucraft.domain.introduce.intro.controller.client;

import com.example.cowmjucraft.domain.introduce.intro.dto.response.IntroduceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Introduce - Public", description = "명지공방 소개 조회 API")
public interface IntroduceControllerDocs {

    @Operation(
            summary = "명지공방 소개 조회",
            description = "명지공방 소개 내용을 조회합니다. 이미지가 필요하면 응답의 mediaId로 /api/media/{id}/url을 호출하세요."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = IntroduceResponseDto.class))
            )
    })
    IntroduceResponseDto getIntroduceContent();
}
