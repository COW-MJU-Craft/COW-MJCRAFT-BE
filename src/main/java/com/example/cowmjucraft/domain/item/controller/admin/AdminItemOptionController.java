package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionGroupResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionValueResponseDto;
import com.example.cowmjucraft.domain.item.service.AdminItemOptionService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/items/{itemId}/option-groups")
public class AdminItemOptionController implements AdminItemOptionControllerDocs {

    private final AdminItemOptionService adminItemOptionService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<List<AdminItemOptionGroupResponseDto>>> getOptionGroups(
            @PathVariable Long itemId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemOptionService.getOptionGroups(itemId));
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResult<AdminItemOptionGroupResponseDto>> createOptionGroup(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminItemOptionGroupCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, adminItemOptionService.createOptionGroup(itemId, request));
    }

    @PutMapping("/{groupId}")
    @Override
    public ResponseEntity<ApiResult<AdminItemOptionGroupResponseDto>> updateOptionGroup(
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody AdminItemOptionGroupUpdateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminItemOptionService.updateOptionGroup(itemId, groupId, request));
    }

    @DeleteMapping("/{groupId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteOptionGroup(
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        adminItemOptionService.deleteOptionGroup(itemId, groupId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }

    @PostMapping("/{groupId}/values")
    @Override
    public ResponseEntity<ApiResult<AdminItemOptionValueResponseDto>> createOptionValue(
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody AdminItemOptionValueCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, adminItemOptionService.createOptionValue(itemId, groupId, request));
    }

    @PutMapping("/{groupId}/values/{valueId}")
    @Override
    public ResponseEntity<ApiResult<AdminItemOptionValueResponseDto>> updateOptionValue(
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long valueId,
            @Valid @RequestBody AdminItemOptionValueUpdateRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminItemOptionService.updateOptionValue(itemId, groupId, valueId, request)
        );
    }

    @DeleteMapping("/{groupId}/values/{valueId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteOptionValue(
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long valueId
    ) {
        adminItemOptionService.deleteOptionValue(itemId, groupId, valueId);
        return ApiResponse.of(SuccessType.MEDIA_DELETED);
    }
}
