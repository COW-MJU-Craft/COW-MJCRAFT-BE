package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemImageOrderPatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.domain.item.service.AdminItemService;
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
@RequestMapping("/api/admin")
public class AdminItemController implements AdminItemControllerDocs {

    private final AdminItemService adminItemService;

    @PostMapping("/projects/{projectId}/items")
    @Override
    public ResponseEntity<ApiResult<AdminProjectItemResponseDto>> createItem(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminProjectItemCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, adminItemService.create(projectId, request));
    }

    @GetMapping("/projects/{projectId}/items")
    @Override
    public ResponseEntity<ApiResult<List<AdminProjectItemResponseDto>>> getItems(
            @PathVariable Long projectId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemService.getItems(projectId));
    }

    @GetMapping("/items/{itemId}")
    @Override
    public ResponseEntity<ApiResult<AdminProjectItemDetailResponseDto>> getItem(
            @PathVariable Long itemId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemService.getItem(itemId));
    }

    @PutMapping("/items/{itemId}")
    @Override
    public ResponseEntity<ApiResult<AdminProjectItemResponseDto>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminProjectItemUpdateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemService.update(itemId, request));
    }

    @PostMapping("/items/{itemId}/thumbnail/presign-put")
    @Override
    public ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignThumbnail(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemPresignPutBatchRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminItemService.createThumbnailPresignPutBatch(itemId, request)
        );
    }

    @PostMapping("/items/{itemId}/images/presign-put")
    @Override
    public ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignImage(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemPresignPutBatchRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminItemService.createImagePresignPutBatch(itemId, request)
        );
    }

    @PostMapping("/projects/{projectId}/journals/presign-put")
    @Override
    public ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignJournalFile(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminItemPresignPutBatchRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.MEDIA_PRESIGN_CREATED,
                adminItemService.createJournalFilePresignPutBatch(projectId, request)
        );
    }

    @DeleteMapping("/items/{itemId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteItem(
            @PathVariable Long itemId
    ) {
        adminItemService.delete(itemId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }

    @DeleteMapping("/items/{itemId}/thumbnail")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteThumbnail(
            @PathVariable Long itemId
    ) {
        adminItemService.deleteThumbnail(itemId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }

    @DeleteMapping("/items/{itemId}/journal")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteJournalFile(
            @PathVariable Long itemId
    ) {
        adminItemService.deleteJournalFile(itemId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }

    @PostMapping("/items/{itemId}/images")
    @Override
    public ResponseEntity<ApiResult<List<ProjectItemImageResponseDto>>> addImages(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemImageCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemService.addImages(itemId, request));
    }

    @PatchMapping("/items/{itemId}/images/order")
    @Override
    public ResponseEntity<ApiResult<AdminItemImageOrderPatchResponseDto>> patchImageOrder(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemImageOrderPatchRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemService.patchImageOrder(itemId, request));
    }

    @DeleteMapping("/items/{itemId}/images/{imageId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId
    ) {
        adminItemService.deleteImage(itemId, imageId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }

    @GetMapping("/items/{itemId}/journal/presign-get")
    @Override
    public ResponseEntity<ApiResult<ProjectItemJournalPresignGetResponseDto>> presignJournalDownloadForAdmin(
            @PathVariable Long itemId
    ) {
        return ApiResponse.of(
                SuccessType.MEDIA_PRESIGN_CREATED,
                adminItemService.createJournalPresignGet(itemId)
        );
    }
}
