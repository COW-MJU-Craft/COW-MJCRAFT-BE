package com.example.cowmjucraft.domain.project.controller.admin;

import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectOrderPatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectOrderPatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.service.AdminProjectService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectController implements AdminProjectControllerDocs {

    private final AdminProjectService adminProjectService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResult<AdminProjectResponseDto>> createProject(
            @Valid @RequestBody AdminProjectCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, adminProjectService.create(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<List<AdminProjectResponseDto>>> getProjects() {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.getProjects());
    }

    @GetMapping("/{projectId}")
    @Override
    public ResponseEntity<ApiResult<AdminProjectResponseDto>> getProject(
            @PathVariable Long projectId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.getProject(projectId));
    }

    @PostMapping("/thumbnail/presign-put")
    @Override
    public ResponseEntity<ApiResult<AdminProjectPresignPutBatchResponseDto>> presignThumbnail(
            @Valid @RequestBody AdminProjectPresignPutBatchRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.createThumbnailPresignPutBatch(request));
    }

    @PostMapping("/images/presign-put")
    @Override
    public ResponseEntity<ApiResult<AdminProjectPresignPutBatchResponseDto>> presignImages(
            @Valid @RequestBody AdminProjectPresignPutBatchRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.createImagePresignPutBatch(request));
    }

    @PutMapping("/{projectId}")
    @Override
    public ResponseEntity<ApiResult<AdminProjectResponseDto>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminProjectUpdateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.update(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteProject(
            @PathVariable Long projectId
    ) {
        adminProjectService.delete(projectId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @PatchMapping("/order")
    @Override
    public ResponseEntity<ApiResult<AdminProjectOrderPatchResponseDto>> patchOrder(
            @Valid @RequestBody AdminProjectOrderPatchRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectService.patchOrder(request));
    }
}
