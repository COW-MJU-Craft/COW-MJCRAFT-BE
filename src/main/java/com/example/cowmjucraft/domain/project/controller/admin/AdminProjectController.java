package com.example.cowmjucraft.domain.project.controller.admin;

import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectOrderPatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectOrderPatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.service.AdminProjectService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
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
    public ApiResult<AdminProjectResponseDto> createProject(
            @Valid @RequestBody AdminProjectCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, adminProjectService.create(request));
    }

    @GetMapping
    @Override
    public ApiResult<List<AdminProjectResponseDto>> getProjects() {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.getProjects());
    }

    @GetMapping("/{projectId}")
    @Override
    public ApiResult<AdminProjectResponseDto> getProject(
            @PathVariable Long projectId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.getProject(projectId));
    }

    @PostMapping("/thumbnail/presign-put")
    @Override
    public ApiResult<AdminProjectPresignPutBatchResponseDto> presignThumbnail(
            @Valid @RequestBody AdminProjectPresignPutBatchRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.createThumbnailPresignPutBatch(request));
    }

    @PostMapping("/images/presign-put")
    @Override
    public ApiResult<AdminProjectPresignPutBatchResponseDto> presignImages(
            @Valid @RequestBody AdminProjectPresignPutBatchRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.createImagePresignPutBatch(request));
    }

    @PutMapping("/{projectId}")
    @Override
    public ApiResult<AdminProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminProjectUpdateRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.update(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @Override
    public ApiResult<?> deleteProject(
            @PathVariable Long projectId
    ) {
        adminProjectService.delete(projectId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @PatchMapping("/order")
    @Override
    public ApiResult<AdminProjectOrderPatchResponseDto> patchOrder(
            @Valid @RequestBody AdminProjectOrderPatchRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminProjectService.patchOrder(request));
    }
}
