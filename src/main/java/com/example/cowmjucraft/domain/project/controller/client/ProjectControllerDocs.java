package com.example.cowmjucraft.domain.project.controller.client;

import com.example.cowmjucraft.domain.project.dto.response.ProjectDetailResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<ProjectDetailResponseDto> getProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );
}
