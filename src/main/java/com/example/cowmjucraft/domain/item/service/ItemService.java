package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemOptionGroupResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemOptionValueResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.exception.ItemErrorType;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ProjectRepository projectRepository;
    private final ProjectItemRepository projectItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ItemOptionGroupRepository itemOptionGroupRepository;
    private final ItemOptionValueRepository itemOptionValueRepository;
    private final S3PresignFacade s3PresignFacade;

    @Transactional(readOnly = true)
    public List<ProjectItemListResponseDto> getItems(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ItemException(ItemErrorType.PROJECT_NOT_FOUND));
        if (project.isDeleted()) {
            throw new ItemException(ItemErrorType.PROJECT_NOT_FOUND);
        }

        List<ProjectItem> items = projectItemRepository.findByProjectIdOrderByCreatedAtDescIdDesc(project.getId());
        Set<String> keySet = new LinkedHashSet<>();
        for (ProjectItem item : items) {
            addIfValidKey(keySet, item.getThumbnailKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);
        Map<Long, List<ProjectItemOptionGroupResponseDto>> optionsByItemId = fetchOptionsByItemIds(
                items.stream().map(ProjectItem::getId).toList()
        );
        return items.stream()
                .map(item -> toListResponse(item, urls, optionsByItemId.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectItemDetailResponseDto getItem(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
        if (item.isDeleted()) {
            throw new ItemException(ItemErrorType.ITEM_NOT_FOUND);
        }

        List<ItemImage> itemImages = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId);
        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, item.getThumbnailKey());
        for (ItemImage image : itemImages) {
            addIfValidKey(keySet, image.getImageKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        List<ProjectItemImageResponseDto> enrichedImages = itemImages.stream()
                .map(image -> new ProjectItemImageResponseDto(
                        image.getId(),
                        resolveUrl(urls, image.getImageKey()),
                        image.getSortOrder()
                ))
                .toList();

        List<ProjectItemOptionGroupResponseDto> options = fetchOptionsByItemIds(List.of(itemId))
                .getOrDefault(itemId, List.of());

        return toDetailResponse(item, enrichedImages, urls, options);
    }

    @Transactional(readOnly = true)
    public ProjectItemJournalPresignGetResponseDto createJournalPresignGet(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
        if (item.isDeleted()) {
            throw new ItemException(ItemErrorType.ITEM_NOT_FOUND);
        }
        if (item.getItemType() != ItemType.DIGITAL_JOURNAL) {
            throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "itemType must be DIGITAL_JOURNAL");
        }
        String key = toNonBlankString(item.getJournalFileKey());
        if (key == null) {
            throw new ItemException(ItemErrorType.JOURNAL_FILE_KEY_REQUIRED);
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
            throw new ItemException(ItemErrorType.PRESIGN_FAILED);
        }
    }

    private ProjectItemListResponseDto toListResponse(
            ProjectItem item,
            Map<String, String> urls,
            List<ProjectItemOptionGroupResponseDto> options
    ) {
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
                resolveUrl(urls, item.getThumbnailKey()),
                stockQty,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty(),
                options
        );
    }

    private ProjectItemDetailResponseDto toDetailResponse(
            ProjectItem item,
            List<ProjectItemImageResponseDto> images,
            Map<String, String> urls,
            List<ProjectItemOptionGroupResponseDto> options
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
                resolveUrl(urls, item.getThumbnailKey()),
                stockQty,
                images,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty(),
                options
        );
    }

    private Map<Long, List<ProjectItemOptionGroupResponseDto>> fetchOptionsByItemIds(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }

        List<ItemOptionGroup> groups = itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(itemIds);
        List<Long> groupIds = groups.stream().map(ItemOptionGroup::getId).toList();
        Map<Long, List<ItemOptionValue>> valuesByGroupId = itemOptionValueRepository
                .findByOptionGroupIdInOrderBySortOrderAsc(groupIds).stream()
                .collect(Collectors.groupingBy(value -> value.getOptionGroup().getId()));

        // groups는 이미 sortOrder 오름차순으로 조회되므로, groupingBy 결과의 그룹별 리스트도
        // 원래 스트림 순서(=sortOrder 순)를 그대로 유지한다.
        return groups.stream()
                .collect(Collectors.groupingBy(
                        group -> group.getItem().getId(),
                        Collectors.mapping(
                                group -> toOptionGroupResponse(group, valuesByGroupId.getOrDefault(group.getId(), List.of())),
                                Collectors.toList()
                        )
                ));
    }

    private ProjectItemOptionGroupResponseDto toOptionGroupResponse(ItemOptionGroup group, List<ItemOptionValue> values) {
        return new ProjectItemOptionGroupResponseDto(
                group.getId(),
                group.getName(),
                group.isRequired(),
                values.stream()
                        .map(value -> new ProjectItemOptionValueResponseDto(
                                value.getId(),
                                value.getName(),
                                value.getAdditionalPrice(),
                                value.getStockQty()
                        ))
                        .toList()
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
        Integer fundedQty = item.getFundedQty();
        int fundedQtyValue = fundedQty == null ? 0 : fundedQty;
        double rate = 0.0;
        if (targetQty != null && targetQty > 0) {
            rate = (double) fundedQtyValue / targetQty * 100.0;
        }
        int remainingQty = targetQty == null ? 0 : Math.max(targetQty - fundedQtyValue, 0);
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
