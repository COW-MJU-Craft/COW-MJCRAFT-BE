package com.example.cowmjucraft.domain.introduce.controller.client;

import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceDetailResponse;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceMainSummaryResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Introduce - Public", description = "소개 조회 API")
public interface IntroduceControllerDocs {

    @Operation(
            summary = "소개 메인 요약 조회",
            description = "메인 화면 요약 정보 및 히어로 로고 key 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<IntroduceMainSummaryResponse> getMainSummary();

    @Operation(
            summary = "소개 상세 조회",
            description = "소개 상세 섹션 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<IntroduceDetailResponse> getDetail();
}
