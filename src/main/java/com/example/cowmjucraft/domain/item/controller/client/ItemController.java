package com.example.cowmjucraft.domain.item.controller.client;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.service.ItemService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<List<ProjectItemListResponseDto>>> getProjectItems(
            @PathVariable Long projectId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, itemService.getItems(projectId));
    }

    @GetMapping("/items/{itemId}")
    @Override
    public ResponseEntity<ApiResult<ProjectItemDetailResponseDto>> getItem(
            @PathVariable Long itemId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, itemService.getItem(itemId));
    }

    @PostMapping("/items/{itemId}/journal/presign-get")
    @Override
    public ResponseEntity<ApiResult<ProjectItemJournalPresignGetResponseDto>> presignJournalDownload(
            @PathVariable Long itemId
    ) {
        return ApiResponse.of(SuccessType.MEDIA_PRESIGN_CREATED, itemService.createJournalPresignGet(itemId));
    }
}
