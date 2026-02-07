package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.entity.ItemType;
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

    @Transactional(readOnly = true)
    public ProjectItemJournalPresignGetResponseDto createJournalPresignGet(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));
        if (item.getItemType() != ItemType.DIGITAL_JOURNAL) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "itemType must be DIGITAL_JOURNAL"
            );
        }
        String key = toNonBlankString(item.getJournalFileKey());
        if (key == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "journalFileKey is required"
            );
        }
        String downloadFileName = buildJournalDownloadFileName(item, key);
        String contentType = isPdfKey(key) ? "application/pdf" : null;
        try {
            String url = s3PresignFacade.presignGet(
                    key,
                    downloadFileName,
                    contentType,
                    true
            );
            return new ProjectItemJournalPresignGetResponseDto(url);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "presign get failed"
            );
        }
    }

    private ProjectItemListResponseDto toListResponse(ProjectItem item, Map<String, String> urls) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        Integer stockQty = item.getSaleType() == ItemSaleType.NORMAL ? item.getStockQty() : null;
        return new ProjectItemListResponseDto(
                item.getId(),
                item.getName(),
                item.getSummary(),
                item.getPrice(),
                item.getSaleType(),
                item.getItemType(),
                item.getStatus(),
                item.getThumbnailKey(),
                resolveUrl(urls, item.getThumbnailKey()),
                stockQty,
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
        Integer stockQty = item.getSaleType() == ItemSaleType.NORMAL ? item.getStockQty() : null;
        return new ProjectItemDetailResponseDto(
                item.getId(),
                item.getProject().getId(),
                item.getName(),
                item.getSummary(),
                item.getDescription(),
                item.getPrice(),
                item.getSaleType(),
                item.getItemType(),
                item.getStatus(),
                item.getThumbnailKey(),
                resolveUrl(urls, item.getThumbnailKey()),
                stockQty,
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

    private String buildJournalDownloadFileName(ProjectItem item, String journalKey) {
        String name = toNonBlankString(item.getName());
        if (name == null) {
            name = "journal";
        }
        String extension = extractExtension(journalKey);
        if (extension != null && !name.endsWith(extension)) {
            return name + extension;
        }
        return name;
    }

    private String extractExtension(String key) {
        String normalized = toNonBlankString(key);
        if (normalized == null) {
            return null;
        }
        int slash = normalized.lastIndexOf('/');
        String base = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = base.lastIndexOf('.');
        if (dot < 1 || dot == base.length() - 1) {
            return null;
        }
        return base.substring(dot);
    }

    private boolean isPdfKey(String key) {
        String extension = extractExtension(key);
        return extension != null && extension.equalsIgnoreCase(".pdf");
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
