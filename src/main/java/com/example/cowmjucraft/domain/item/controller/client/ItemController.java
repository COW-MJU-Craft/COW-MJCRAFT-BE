package com.example.cowmjucraft.domain.item.controller.client;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.service.ItemService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ItemController implements ItemControllerDocs {

    private final ItemService itemService;

    @GetMapping("/projects/{projectId}/items")
    @Override
    public ApiResult<List<ProjectItemListResponseDto>> getProjectItems(
            @PathVariable Long projectId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, itemService.getItems(projectId));
    }

    @GetMapping("/items/{itemId}")
    @Override
    public ApiResult<ProjectItemDetailResponseDto> getItem(
            @PathVariable Long itemId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, itemService.getItem(itemId));
    }

    @PostMapping("/items/{itemId}/journal/presign-get")
    @Override
    public ApiResult<ProjectItemJournalPresignGetResponseDto> presignJournalDownload(
            @PathVariable Long itemId
    ) {
        return ApiResult.success(SuccessType.MEDIA_PRESIGN_CREATED, itemService.createJournalPresignGet(itemId));
    }
}
