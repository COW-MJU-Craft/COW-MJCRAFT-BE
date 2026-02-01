package com.example.cowmjucraft.domain.project.controller.client;

import com.example.cowmjucraft.domain.project.dto.response.ProjectDetailResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Project - Public", description = "프로젝트 공개 조회 API")
public interface ProjectControllerDocs {

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "고정/마감일/수동정렬 규칙에 따라 프로젝트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            )
    })
    ApiResult<List<ProjectListItemResponseDto>> getProjects();

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "프로젝트 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "detail-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
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
                                                "dDay": 30,
                                                "createdAt": "2026-01-20T10:00:00",
                                                "updatedAt": "2026-01-29T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<ProjectDetailResponseDto> getProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );
}
