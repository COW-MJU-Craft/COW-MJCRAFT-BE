package com.example.cowmjucraft.domain.project.controller.admin;

import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectOrderPatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectOrderPatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Project - Admin", description = "프로젝트 관리자 API")
public interface AdminProjectControllerDocs {

    @Operation(
            summary = "프로젝트 전체 조회",
            description = "프로젝트 목록을 관리자 관점으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            )
    })
    ApiResult<List<AdminProjectResponseDto>> getProjects();

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "프로젝트 상세를 관리자 관점으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<AdminProjectResponseDto> getProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );

    @Operation(
            summary = "프로젝트 생성",
            description = "프로젝트 기본 정보를 생성합니다."
    )
    @RequestBody(
            required = true,
            description = "프로젝트 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "create-request",
                            value = """
                                    {
                                      "title": "명지공방 머그컵 프로젝트",
                                      "summary": "캠퍼스 감성을 담은 머그컵을 제작합니다.",
                                      "description": "학생들이 함께 디자인한 머그컵 프로젝트입니다.",
                                      "thumbnailKey": "uploads/projects/thumbnails/uuid-thumbnail.png",
                                      "imageKeys": [
                                        "uploads/projects/images/uuid-01.png",
                                        "uploads/projects/images/uuid-02.png"
                                      ],
                                      "deadlineDate": "2026-03-15",
                                      "status": "OPEN"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "create-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 201,
                                              "message": "성공적으로 생성하였습니다.",
                                              "data": {
                                                "id": 1,
                                                "title": "명지공방 머그컵 프로젝트",
                                                "summary": "캠퍼스 감성을 담은 머그컵을 제작합니다.",
                                                "description": "학생들이 함께 디자인한 머그컵 프로젝트입니다.",
                                                "thumbnailKey": "uploads/projects/thumbnails/uuid-thumbnail.png",
                                                "thumbnailUrl": "https://bucket.s3.amazonaws.com/uploads/projects/thumbnails/uuid-thumbnail.png?X-Amz-Signature=...",
                                                "imageKeys": [
                                                  "uploads/projects/images/uuid-01.png",
                                                  "uploads/projects/images/uuid-02.png"
                                                ],
                                                "imageUrls": [
                                                  "https://bucket.s3.amazonaws.com/uploads/projects/images/uuid-01.png?X-Amz-Signature=...",
                                                  "https://bucket.s3.amazonaws.com/uploads/projects/images/uuid-02.png?X-Amz-Signature=..."
                                                ],
                                                "status": "OPEN",
                                                "deadlineDate": "2026-03-15",
                                                "pinned": false,
                                                "pinnedOrder": null,
                                                "manualOrder": null,
                                                "createdAt": "2026-01-29T10:00:00",
                                                "updatedAt": "2026-01-29T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminProjectResponseDto> createProject(
            @Valid AdminProjectCreateRequestDto request
    );

    @Operation(
            summary = "프로젝트 썸네일 presign-put 배치 발급",
            description = "프로젝트 썸네일 업로드용 presigned PUT URL을 여러 건 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "presign-put 배치 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectPresignPutBatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "thumbnail-presign-request",
                            value = """
                                    {
                                      "files": [
                                        { "fileName": "thumbnail-1.png", "contentType": "image/png" },
                                        { "fileName": "thumbnail-2.png", "contentType": "image/png" }
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
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                            name = "thumbnail-presign-response",
                            value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "items": [
                                                  {
                                                    "fileName": "thumbnail-1.png",
                                                    "key": "uploads/projects/thumbnails/uuid-thumbnail-1.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  },
                                                  {
                                                    "fileName": "thumbnail-2.png",
                                                    "key": "uploads/projects/thumbnails/uuid-thumbnail-2.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<AdminProjectPresignPutBatchResponseDto> presignThumbnail(
            @Valid AdminProjectPresignPutBatchRequestDto request
    );

    @Operation(
            summary = "프로젝트 상세 이미지 presign-put 배치 발급",
            description = "프로젝트 상세 이미지 업로드용 presigned PUT URL을 여러 건 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "presign-put 배치 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectPresignPutBatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "images-presign-request",
                            value = """
                                    {
                                      "files": [
                                        { "fileName": "detail-1.png", "contentType": "image/png" },
                                        { "fileName": "detail-2.png", "contentType": "image/png" }
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
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                            name = "images-presign-response",
                            value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "items": [
                                                  {
                                                    "fileName": "detail-1.png",
                                                    "key": "uploads/projects/images/uuid-detail-1.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  },
                                                  {
                                                    "fileName": "detail-2.png",
                                                    "key": "uploads/projects/images/uuid-detail-2.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<AdminProjectPresignPutBatchResponseDto> presignImages(
            @Valid AdminProjectPresignPutBatchRequestDto request
    );

    @Operation(
            summary = "프로젝트 수정",
            description = "프로젝트 기본 정보를 수정합니다."
    )
    @RequestBody(
            required = true,
            description = "프로젝트 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectUpdateRequestDto.class),
                    examples = @ExampleObject(
                            name = "update-request",
                            value = """
                                    {
                                      "title": "명지공방 머그컵 프로젝트 (리뉴얼)",
                                      "summary": "굿즈 라인업을 확장합니다.",
                                      "description": "학생들이 함께 디자인한 머그컵 프로젝트입니다.",
                                      "thumbnailKey": "uploads/projects/thumbnails/uuid-thumbnail.png",
                                      "imageKeys": [
                                        "uploads/projects/images/uuid-01.png",
                                        "uploads/projects/images/uuid-02.png"
                                      ],
                                      "deadlineDate": "2026-03-20",
                                      "status": "OPEN"
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
                                    name = "update-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "id": 1,
                                                "title": "명지공방 머그컵 프로젝트 (리뉴얼)",
                                                "summary": "굿즈 라인업을 확장합니다.",
                                                "description": "학생들이 함께 디자인한 머그컵 프로젝트입니다.",
                                                "thumbnailKey": "uploads/projects/thumbnails/uuid-thumbnail.png",
                                                "thumbnailUrl": "https://bucket.s3.amazonaws.com/uploads/projects/thumbnails/uuid-thumbnail.png?X-Amz-Signature=...",
                                                "imageKeys": [
                                                  "uploads/projects/images/uuid-01.png",
                                                  "uploads/projects/images/uuid-02.png"
                                                ],
                                                "imageUrls": [
                                                  "https://bucket.s3.amazonaws.com/uploads/projects/images/uuid-01.png?X-Amz-Signature=...",
                                                  "https://bucket.s3.amazonaws.com/uploads/projects/images/uuid-02.png?X-Amz-Signature=..."
                                                ],
                                                "status": "OPEN",
                                                "deadlineDate": "2026-03-20",
                                                "pinned": false,
                                                "pinnedOrder": null,
                                                "manualOrder": null,
                                                "createdAt": "2026-01-20T10:00:00",
                                                "updatedAt": "2026-01-29T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminProjectResponseDto> updateProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Valid AdminProjectUpdateRequestDto request
    );

    @Operation(
            summary = "프로젝트 삭제",
            description = "프로젝트를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 204,
                                              "message": "요청에 성공하였습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<?> deleteProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );

    @Operation(
            summary = "프로젝트 고정/정렬 일괄 변경",
            description = """
                    고정 여부 및 정렬 순서를 일괄로 반영합니다.
                    items에 포함된 프로젝트만 갱신되며, 포함되지 않은 프로젝트는 기존 값을 유지합니다.
                    pinned 집합 변경(고정 추가/해제)을 허용합니다.
                    pinned=false & manualOrder=null 인 경우 자동정렬 대상으로 저장됩니다.
                    manualOrder는 1 이상이며 pinned=true일 때는 manualOrder를 보낼 수 없습니다.
                    pinned=true일 때 pinnedOrder는 선택값이며 1 이상일 경우 pinned=true 항목 내에서 중복 불가합니다.
                    pinnedOrder는 pinned=true 전체 프로젝트에 대해 서버가 1..N으로 재부여합니다.
                    요청 pinnedOrder는 pinned=true 항목의 상대적 우선순위로만 사용됩니다.
                    """
    )
    @RequestBody(
            required = true,
            description = "프로젝트 고정/정렬 변경 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectOrderPatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "order-request",
                            value = """
                                    {
                                      "items": [
                                        { "projectId": 3, "pinned": true, "pinnedOrder": 1 },
                                        { "projectId": 1, "pinned": true, "pinnedOrder": 2 },
                                        { "projectId": 7, "pinned": true, "pinnedOrder": 3 },
                                        { "projectId": 10, "pinned": false, "manualOrder": 1 },
                                        { "projectId": 12, "pinned": false, "manualOrder": 2 }
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
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "order-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "updatedPinnedCount": 3,
                                                "updatedManualCount": 2
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminProjectOrderPatchResponseDto> patchOrder(
            @Valid AdminProjectOrderPatchRequestDto request
    );
}
