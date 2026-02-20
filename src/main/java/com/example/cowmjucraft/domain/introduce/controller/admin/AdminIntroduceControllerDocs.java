package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceDetailUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceMainUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceMainResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
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
            summary = "소개 메인 원본 조회",
            description = "관리자 편집용 메인(홈) 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ResponseEntity<ApiResult<AdminIntroduceMainResponseDto>> getMain();

    @Operation(
            summary = "소개 메인 원본 저장",
            description = """
                    메인 페이지 데이터를 전체 덮어쓰기 방식으로 저장합니다.
                    
                    - heroLogoKeys: 메인 페이지(히어로 영역)에 노출되는 로고 이미지 key 목록
                    """
    )
    @RequestBody(
            required = true,
            description = "소개 메인 저장 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminIntroduceMainUpsertRequestDto.class),
                    examples = @ExampleObject(
                            name = "introduce-main-upsert-request",
                            value = """
                                    {
                                      "title": "명지공방",
                                      "subtitle": "학생들이 만드는 작은 브랜드",
                                      "summary": "명지공방은 ...",
                                      "heroLogoKeys": [
                                        "uploads/introduce/hero-logos/uuid-hero-1.png",
                                        "uploads/introduce/hero-logos/uuid-hero-2.png"
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
    ResponseEntity<ApiResult<AdminIntroduceMainResponseDto>> upsertMain(
            @Valid AdminIntroduceMainUpsertRequestDto request
    );

    @Operation(
            summary = "소개 상세 원본 조회",
            description = "관리자 편집용 상세(소개) 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ResponseEntity<ApiResult<AdminIntroduceDetailResponseDto>> getDetail();

    @Operation(
            summary = "소개 상세 원본 저장",
            description = """
                    소개 상세 데이터를 전체 덮어쓰기 방식으로 저장합니다.

                    - purpose: 목적(Purpose)
                    - currentLogo: 현재 로고
                    - logoHistories: 과거 로고 목록
                    """
    )
    @RequestBody(
            required = true,
            description = "소개 상세 저장 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminIntroduceDetailUpsertRequestDto.class),
                    examples = @ExampleObject(
                            name = "introduce-detail-upsert-request",
                            value = """
                                    {
                                      "intro": {
                                        "title": "명지공방(明智工房)",
                                        "slogan": "우리의 손끝에서, 명지가 피어납니다.",
                                        "body": "안녕하세요!\\n명지공방은..."
                                      },
                                      "purpose": {
                                        "title": "우리가 하는 일",
                                        "description": "명지공방은 학생들이 직접 기획하고 제작하는 굿즈 프로젝트를 운영합니다."
                                      },
                                      "currentLogo": {
                                        "title": "현재 로고",
                                        "imageKey": "uploads/introduce/sections/uuid-current-logo.png",
                                        "description": "현재 사용 중인 로고입니다."
                                      },
                                      "logoHistories": [
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
    ResponseEntity<ApiResult<AdminIntroduceDetailResponseDto>> upsertDetail(
            @Valid AdminIntroduceDetailUpsertRequestDto request
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
    ResponseEntity<ApiResult<AdminIntroducePresignPutResponseDto>> presignHeroLogo(
            @Valid AdminIntroducePresignPutRequestDto request
    );

    @Operation(
            summary = "소개 섹션 이미지 presign-put 발급",
            description = """
                    소개 상세 페이지 섹션에서 사용되는 이미지를 업로드하기 위한
                    S3 presigned PUT URL을 발급합니다.

                    - currentLogo, logoHistories 이미지에 사용됩니다.
                    - 업로드 완료 후 반환된 key를 상세 upsert API의 imageKey로 저장하세요.
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
    ResponseEntity<ApiResult<AdminIntroducePresignPutResponseDto>> presignSection(
            @Valid AdminIntroducePresignPutRequestDto request
    );
}
