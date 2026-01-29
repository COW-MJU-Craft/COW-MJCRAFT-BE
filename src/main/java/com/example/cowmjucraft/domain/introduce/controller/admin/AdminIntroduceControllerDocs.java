package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Introduce - Admin", description = "소개 관리자 API")
public interface AdminIntroduceControllerDocs {

    @Operation(
            summary = "소개 원본 조회",
            description = "관리자 편집용 원본 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<AdminIntroduceResponseDto> getIntroduce();

    @Operation(
            summary = "소개 원본 저장",
            description = """
                    소개 데이터를 전체 덮어쓰기 방식으로 저장합니다.

                    - heroLogoKeys: 메인 페이지(히어로 영역)에 노출되는 로고 이미지 key 목록
                    - sections: 소개 상세 페이지 섹션 목록(type 포함)
                    """
    )
    @RequestBody(
            required = true,
            description = "소개 저장(전체 덮어쓰기) 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminIntroduceUpsertRequestDto.class),
                    examples = @ExampleObject(
                            name = "introduce-upsert-request",
                            value = """
                                    {
                                      "title": "명지공방 소개",
                                      "subtitle": "우리의 방향과 비전",
                                      "summary": "명지공방은 ...",
                                      "heroLogoKeys": [
                                        "uploads/introduce/hero-logos/uuid-hero-1.png",
                                        "uploads/introduce/hero-logos/uuid-hero-2.png"
                                      ],
                                      "sections": [
                                        {
                                          "type": "HEADER",
                                          "headline": "명지공방",
                                          "description": "학교와 지역을 잇는 공방 프로젝트"
                                        },
                                        {
                                          "type": "PURPOSE",
                                          "title": "우리가 하는 일",
                                          "items": [
                                            { "title": "제작", "description": "기획부터 제작까지" },
                                            { "title": "연결", "description": "학생과 지역을 연결" }
                                          ]
                                        },
                                        {
                                          "type": "CURRENT_LOGO",
                                          "title": "현재 로고",
                                          "imageKey": "uploads/introduce/sections/uuid-current-logo.png",
                                          "description": "현재 사용 중인 로고입니다."
                                        },
                                        {
                                          "type": "LOGO_HISTORY",
                                          "title": "로고 히스토리",
                                          "items": [
                                            {
                                              "year": "2023",
                                              "imageKey": "uploads/introduce/sections/uuid-logo-2023.png",
                                              "description": "2023 로고"
                                            },
                                            {
                                              "year": "2024",
                                              "imageKey": "uploads/introduce/sections/uuid-logo-2024.png",
                                              "description": "2024 로고"
                                            }
                                          ]
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<AdminIntroduceResponseDto> upsertIntroduce(
            @Valid  AdminIntroduceUpsertRequestDto request
    );

    @Operation(
            summary = "소개 메인 히어로 로고 presign-put 발급",
            description = """
                    메인 페이지(히어로 영역)에 노출되는 로고 이미지를 업로드하기 위한
                    S3 presigned PUT URL을 발급합니다.

                    - 여러 개의 로고를 등록할 수 있습니다.
                    - 업로드 완료 후 반환된 key를 upsert API의 heroLogoKeys에 저장하세요.
                    """
    )
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminIntroducePresignPutRequestDto.class),
                    examples = @ExampleObject(
                            name = "introduce-hero-logo-presign-request",
                            value = """
                                    {
                                      "fileName": "hero-logo.png",
                                      "contentType": "image/png"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "introduce-hero-logo-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "key": "uploads/introduce/hero-logos/uuid-hero-logo.png",
                                                "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                "expiresInSeconds": 300
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<AdminIntroducePresignPutResponseDto> presignHeroLogo(
            @Valid AdminIntroducePresignPutRequestDto request
    );

    @Operation(
            summary = "소개 섹션 이미지 presign-put 발급",
            description = """
                    소개 상세 페이지 섹션에서 사용되는 이미지를 업로드하기 위한
                    S3 presigned PUT URL을 발급합니다.

                    - CURRENT_LOGO, LOGO_HISTORY 섹션 이미지에 사용됩니다.
                    - 업로드 완료 후 반환된 key를 upsert API의 sections 내부 imageKey로 저장하세요.
                    """
    )
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminIntroducePresignPutRequestDto.class),
                    examples = @ExampleObject(
                            name = "introduce-section-presign-request",
                            value = """
                                    {
                                      "fileName": "section.png",
                                      "contentType": "image/png"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "introduce-section-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "key": "uploads/introduce/sections/uuid-section.png",
                                                "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                "expiresInSeconds": 300
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<AdminIntroducePresignPutResponseDto> presignSection(
            @Valid AdminIntroducePresignPutRequestDto request
    );
}
