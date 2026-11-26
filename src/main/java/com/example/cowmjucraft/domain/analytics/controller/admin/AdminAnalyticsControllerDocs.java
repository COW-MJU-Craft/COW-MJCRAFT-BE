package com.example.cowmjucraft.domain.analytics.controller.admin;

import com.example.cowmjucraft.domain.analytics.dto.response.AdminAnalyticsChannelsResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(name = "Analytics - Admin", description = "GA4 유입 분석 관리자 API")
public interface AdminAnalyticsControllerDocs {

    @Operation(
            summary = "유입 채널별 방문 대비 주문 전환 조회",
            description = "GA4 데이터를 채널(source/medium/campaign)별로 집계해 반환합니다. 같은 기간은 15분간 캐시됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "400", description = "조회 기간이 올바르지 않음"),
            @ApiResponse(responseCode = "502", description = "GA 호출 실패"),
            @ApiResponse(responseCode = "503", description = "GA 연동 설정 없음")
    })
    ResponseEntity<ApiResult<AdminAnalyticsChannelsResponseDto>> getChannels(
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-11-01") LocalDate from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2026-11-26") LocalDate to
    );
}
