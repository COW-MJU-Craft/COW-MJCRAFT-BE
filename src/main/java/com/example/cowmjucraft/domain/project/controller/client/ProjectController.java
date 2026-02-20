package com.example.cowmjucraft.domain.project.controller.client;

import com.example.cowmjucraft.domain.project.dto.response.ProjectDetailResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.domain.project.service.ProjectService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectController implements ProjectControllerDocs {

    private final ProjectService projectService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<List<ProjectListItemResponseDto>>> getProjects() {
        return ApiResponse.of(SuccessType.SUCCESS, projectService.getProjects());
    }

    @GetMapping("/{projectId}")
    @Override
    public ResponseEntity<ApiResult<ProjectDetailResponseDto>> getProject(
            @PathVariable Long projectId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, projectService.getProject(projectId));
    }
}
