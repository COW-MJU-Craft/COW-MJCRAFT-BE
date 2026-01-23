package com.example.cowmjucraft.domain.introduce.sns.controller.client;

import com.example.cowmjucraft.domain.introduce.sns.dto.response.SnsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Tag(name = "Introduce - Public", description = "SNS 소개 조회 API")
public interface SnsControllerDocs {

    @Operation(
            summary = "SNS 링크 조회",
            description = "활성화(active=true)된 SNS 링크를 sortOrder 오름차순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SnsResponseDto.class)))
            )
    })
    List<SnsResponseDto> getSnsLinks();
}
