package com.example.cowmjucraft.domain.sns.controller.client;

import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "SNS - Public", description = "SNS 링크 조회 API")
public interface SnsControllerDocs {

    @Operation(
            summary = "카카오 오픈채팅 링크 조회",
            description = "카카오 오픈채팅 링크를 조회합니다."
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
                                      "message": "요청에 성공하였습니다.",
                                      "data": {
                                        "type": "KAKAO",
                                        "url": "https://open.kakao.com/o/xxxx"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<SnsResponseDto> getKakaoLink();

    @Operation(
            summary = "인스타그램 링크 조회",
            description = "인스타그램 링크를 조회합니다."
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
                                      "message": "요청에 성공하였습니다.",
                                      "data": {
                                        "type": "INSTAGRAM",
                                        "url": "https://instagram.com/mju_craft"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<SnsResponseDto> getInstagramLink();
}
