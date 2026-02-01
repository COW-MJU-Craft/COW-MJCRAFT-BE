package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemPresignPutRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemImageOrderPatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemPresignPutResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.service.AdminItemService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminItemController implements AdminItemControllerDocs {

    private final AdminItemService adminItemService;

    @PostMapping("/projects/{projectId}/items")
    @Override
    public ApiResult<AdminProjectItemResponseDto> createItem(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminProjectItemCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, adminItemService.create(projectId, request));
    }

    @PutMapping("/items/{itemId}")
    @Override
    public ApiResult<AdminProjectItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminProjectItemUpdateRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminItemService.update(itemId, request));
    }

    @PostMapping("/items/{itemId}/thumbnail/presign-put")
    @Override
    public ApiResult<AdminItemPresignPutResponseDto> presignThumbnail(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemPresignPutRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminItemService.createThumbnailPresignPut(itemId, request));
    }

    @PostMapping("/items/{itemId}/images/presign-put")
    @Override
    public ApiResult<AdminItemPresignPutResponseDto> presignImage(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemPresignPutRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminItemService.createImagePresignPut(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Override
    public ApiResult<?> deleteItem(
            @PathVariable Long itemId
    ) {
        adminItemService.delete(itemId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @PostMapping("/items/{itemId}/images")
    @Override
    public ApiResult<List<ProjectItemImageResponseDto>> addImages(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemImageCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminItemService.addImages(itemId, request));
    }

    @PatchMapping("/items/{itemId}/images/order")
    @Override
    public ApiResult<AdminItemImageOrderPatchResponseDto> patchImageOrder(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemImageOrderPatchRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminItemService.patchImageOrder(itemId, request));
    }

    @DeleteMapping("/items/{itemId}/images/{imageId}")
    @Override
    public ApiResult<?> deleteImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId
    ) {
        adminItemService.deleteImage(itemId, imageId);
        return ApiResult.success(SuccessType.SUCCESS);
    }
}
