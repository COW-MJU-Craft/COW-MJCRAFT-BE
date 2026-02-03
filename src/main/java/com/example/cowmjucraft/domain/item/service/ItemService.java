package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ProjectRepository projectRepository;
    private final ProjectItemRepository projectItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final S3PresignFacade s3PresignFacade;

    @Transactional(readOnly = true)
    public List<ProjectItemListResponseDto> getItems(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));

        List<ProjectItem> items = projectItemRepository.findByProjectIdOrderByCreatedAtDescIdDesc(project.getId());
        Set<String> keySet = new LinkedHashSet<>();
        for (ProjectItem item : items) {
            addIfValidKey(keySet, item.getThumbnailKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);
        return items.stream()
                .map(item -> toListResponse(item, urls))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectItemDetailResponseDto getItem(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));

        List<ProjectItemImageResponseDto> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId)
                .stream()
                .map(ProjectItemImageResponseDto::from)
                .toList();
        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, item.getThumbnailKey());
        for (ProjectItemImageResponseDto image : images) {
            addIfValidKey(keySet, image.imageKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        List<ProjectItemImageResponseDto> enrichedImages = images.stream()
                .map(image -> new ProjectItemImageResponseDto(
                        image.id(),
                        image.imageKey(),
                        resolveUrl(urls, image.imageKey()),
                        image.sortOrder()
                ))
                .toList();

        return toDetailResponse(item, enrichedImages, urls);
    }

    private ProjectItemListResponseDto toListResponse(ProjectItem item, Map<String, String> urls) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        return new ProjectItemListResponseDto(
                item.getId(),
                item.getName(),
                item.getSummary(),
                item.getPrice(),
                item.getSaleType(),
                item.getStatus(),
                item.getThumbnailKey(),
                resolveUrl(urls, item.getThumbnailKey()),
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty()
        );
    }

    private ProjectItemDetailResponseDto toDetailResponse(
            ProjectItem item,
            List<ProjectItemImageResponseDto> images,
            Map<String, String> urls
    ) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        return new ProjectItemDetailResponseDto(
                item.getId(),
                item.getProject().getId(),
                item.getName(),
                item.getSummary(),
                item.getDescription(),
                item.getPrice(),
                item.getSaleType(),
                item.getStatus(),
                item.getThumbnailKey(),
                resolveUrl(urls, item.getThumbnailKey()),
                images,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty()
        );
    }

    private Map<String, String> presignGetSafely(Set<String> keys) {
        try {
            return keys == null || keys.isEmpty()
                    ? Map.of()
                    : s3PresignFacade.presignGet(new ArrayList<>(keys));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String resolveUrl(Map<String, String> urls, String key) {
        String k = toNonBlankString(key);
        return k == null ? null : urls.get(k);
    }

    private void addIfValidKey(Set<String> keys, String value) {
        String k = toNonBlankString(value);
        if (k != null) {
            keys.add(k);
        }
    }

    private String toNonBlankString(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private GroupbuyInfo calculateGroupbuyInfo(ProjectItem item) {
        if (item.getSaleType() != ItemSaleType.GROUPBUY) {
            return new GroupbuyInfo(null, null, null, null);
        }
        Integer targetQty = item.getTargetQty();
        int fundedQty = item.getFundedQty();
        double rate = 0.0;
        if (targetQty != null && targetQty > 0) {
            rate = (double) fundedQty / targetQty * 100.0;
        }
        int remainingQty = targetQty == null ? 0 : Math.max(targetQty - fundedQty, 0);
        return new GroupbuyInfo(targetQty, fundedQty, rate, remainingQty);
    }

    private record GroupbuyInfo(
            Integer targetQty,
            Integer fundedQty,
            Double achievementRate,
            Integer remainingQty
    ) {
    }
}
