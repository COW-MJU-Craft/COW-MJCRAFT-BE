package com.example.cowmjucraft.domain.notice.controller.client;

import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Notice", description = "공지사항 조회 API")
public interface NoticeControllerDocs {

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            )
    })
    ResponseEntity<ApiResult<List<NoticeSummaryResponseDto>>> getNotices();

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<NoticeDetailResponseDto>> getNotice(
            @Parameter(description = "공지 ID", example = "1")
            Long noticeId
    );
}
