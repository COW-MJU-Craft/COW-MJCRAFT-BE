package com.example.cowmjucraft.domain.media.controller;

import com.example.cowmjucraft.domain.media.dto.request.MediaDeleteRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignGetRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignPutRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignGetResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignPutResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Media", description = "S3 presign/util API")
public interface MediaPresignControllerDocs {

    @Operation(
            summary = "S3 presign PUT 발급",
            description = "업로드용 presigned PUT URL을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "발급 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<MediaPresignPutResponseDto> presignPut(
            @Valid @RequestBody MediaPresignPutRequestDto request
    );

    @Operation(
            summary = "S3 presign GET 발급",
            description = """
                    S3 object key 목록을 받아 presigned GET URL을 반환합니다.
                    이 API는 도메인 의미(썸네일/상세/로고)를 알지 않고,
                    S3 object key 목록을 presigned GET URL로 변환해 반환합니다.
                    도메인별 분류/매핑은 프론트 또는 도메인 응답에서 처리합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "발급 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<MediaPresignGetResponseDto> presignGet(
            @Valid @RequestBody MediaPresignGetRequestDto request
    );

    @Operation(
            summary = "S3 객체 삭제",
            description = "S3 object key 목록을 받아 객체를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 완료",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<?> delete(
            @Valid @RequestBody MediaDeleteRequestDto request
    );
}