package com.example.cowmjucraft.domain.media.controller.client;

import com.example.cowmjucraft.domain.media.dto.response.MediaMetadataResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Media", description = "미디어 공개 조회 API")
public interface MediaControllerDocs {

    @Operation(
            summary = "공개 미디어 presign URL 조회",
            description = "ACTIVE + PUBLIC 상태의 미디어만 presign GET URL을 발급합니다. presign → 업로드 → activate 이후 공개 조회가 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공", content = @Content(schema = @Schema(implementation = MediaPresignUrlResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "미디어 없음 또는 비공개")
    })
    MediaPresignUrlResponseDto getPresignedUrl(@PathVariable Long mediaId);

    @Operation(
            summary = "공개 미디어 메타데이터 조회",
            description = "ACTIVE + PUBLIC 상태의 메타데이터만 조회됩니다. 비공개/대기/삭제 상태는 404로 응답합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MediaMetadataResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "미디어 없음 또는 비공개")
    })
    MediaMetadataResponseDto getMetadata(@PathVariable Long mediaId);
}
