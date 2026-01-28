package com.example.cowmjucraft.domain.sns.controller.admin;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

@Tag(
        name = "SNS - Admin",
        description = "SNS 링크 관리자 API"
)
public interface SnsAdminControllerDocs {

    @Operation(
            summary = "카카오 오픈채팅 링크 등록/수정",
            description = "카카오 오픈채팅 링크를 등록/수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "resultType": "SUCCESS",
                                      "httpStatusCode": 200,
                                      "message": "요청에 성공하였습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResult<?> upsertKakao(
            @RequestBody(
                    required = true,
                    description = "카카오 오픈채팅 링크 정보",
                    content = @Content(
                            schema = @Schema(implementation = SnsAdminRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "url": "https://open.kakao.com/o/xxxx"
                                    }
                                    """
                            )
                    )
            )
            @Valid
            SnsAdminRequestDto request
    );

    @Operation(
            summary = "인스타그램 링크 등록/수정",
            description = "인스타그램 링크를 등록/수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "resultType": "SUCCESS",
                                      "httpStatusCode": 200,
                                      "message": "요청에 성공하였습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResult<?> upsertInstagram(
            @RequestBody(
                    required = true,
                    description = "인스타그램 링크 정보",
                    content = @Content(
                            schema = @Schema(implementation = SnsAdminRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "url": "https://instagram.com/mju_craft"
                                    }
                                    """
                            )
                    )
            )
            @Valid
            SnsAdminRequestDto request
    );
}
