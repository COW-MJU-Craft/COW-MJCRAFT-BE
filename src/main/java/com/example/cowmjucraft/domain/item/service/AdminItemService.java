package com.example.cowmjucraft.domain.item.service;

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
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.exception.ItemErrorType;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminItemService {

    private final ProjectRepository projectRepository;
    private final ProjectItemRepository projectItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final S3PresignFacade s3PresignFacade;

    @Transactional
    public AdminProjectItemResponseDto create(Long projectId, AdminProjectItemCreateRequestDto request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ItemException(ItemErrorType.PROJECT_NOT_FOUND));

        NormalizedItemRequest normalized = normalizeCreate(project, request);
        ProjectItem item = new ProjectItem(
                project,
                request.name(),
                request.summary(),
                request.description(),
                request.price(),
                request.saleType(),
                request.status(),
                normalized.itemType(),
                normalized.thumbnailKey(),
                normalized.journalFileKey(),
                normalized.targetQty(),
                normalized.fundedQty(),
                normalized.stockQty()
        );
        ProjectItem saved = projectItemRepository.save(item);
        Map<String, String> urls = presignGetForItem(saved);
        return toAdminResponse(saved, urls);
    }

    @Transactional
    public AdminProjectItemResponseDto update(Long itemId, AdminProjectItemUpdateRequestDto request) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));

        NormalizedItemRequest normalized = normalizeUpdate(item.getProject(), item, request);
        item.update(
                request.name(),
                request.summary(),
                request.description(),
                request.price(),
                request.saleType(),
                request.status(),
                normalized.itemType(),
                normalized.thumbnailKey(),
                normalized.journalFileKey(),
                normalized.targetQty(),
                normalized.fundedQty(),
                normalized.stockQty()
        );
        Map<String, String> urls = presignGetForItem(item);
        return toAdminResponse(item, urls);
    }

    public AdminItemPresignPutBatchResponseDto createThumbnailPresignPutBatch(
            Long itemId,
            AdminItemPresignPutBatchRequestDto request
    ) {
        ensureItemExists(itemId);
        String prefix = "uploads/items/" + itemId + "/thumbnail";
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                prefix,
                toPresignFiles(request.files())
        );
        return toBatchPresignResponse(response);
    }

    public AdminItemPresignPutBatchResponseDto createImagePresignPutBatch(
            Long itemId,
            AdminItemPresignPutBatchRequestDto request
    ) {
        ensureItemExists(itemId);
        String prefix = "uploads/items/" + itemId + "/images";
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                prefix,
                toPresignFiles(request.files())
        );
        return toBatchPresignResponse(response);
    }

    public AdminItemPresignPutBatchResponseDto createJournalFilePresignPutBatch(
            Long projectId,
            AdminItemPresignPutBatchRequestDto request
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ItemException(ItemErrorType.PROJECT_NOT_FOUND));
        ProjectCategory category = project.getCategory() == null ? ProjectCategory.GOODS : project.getCategory();
        if (category != ProjectCategory.JOURNAL) {
            throw new ItemException(ItemErrorType.PROJECT_CATEGORY_MISMATCH, "project category must be JOURNAL");
        }

        String prefix = "uploads/projects/" + projectId + "/journals";
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                prefix,
                toPresignFiles(request.files())
        );
        return toBatchPresignResponse(response);
    }

    @Transactional
    public void delete(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
        projectItemRepository.delete(item);
    }

    @Transactional
    public void deleteThumbnail(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));

        String key = toNonBlankString(item.getThumbnailKey());
        if (key != null) {
            try {
                s3PresignFacade.deleteByKeys(List.of(key));
            } catch (Exception e) {
                throw new ItemException(ItemErrorType.S3_DELETE_FAILED);
            }
        }

        item.clearThumbnail();
    }

    @Transactional
    public void deleteJournalFile(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
        if (item.getItemType() != ItemType.DIGITAL_JOURNAL) {
            throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "itemType must be DIGITAL_JOURNAL");
        }

        String key = toNonBlankString(item.getJournalFileKey());
        if (key == null) {
            throw new ItemException(ItemErrorType.JOURNAL_FILE_KEY_REQUIRED);
        }

        try {
            s3PresignFacade.deleteByKeys(List.of(key));
        } catch (Exception e) {
            throw new ItemException(ItemErrorType.S3_DELETE_FAILED);
        }

        item.clearJournalFileKey();
    }

    @Transactional(readOnly = true)
    public List<AdminProjectItemResponseDto> getItems(Long projectId) {
        List<ProjectItem> items = projectItemRepository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId);
        Set<String> keySet = new LinkedHashSet<>();
        for (ProjectItem item : items) {
            addIfValidKey(keySet, item.getThumbnailKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        return items.stream()
                .map(item -> toAdminResponse(item, urls))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProjectItemDetailResponseDto getItem(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
        List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId);

        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, item.getThumbnailKey());
        for (ItemImage image : images) {
            addIfValidKey(keySet, image.getImageKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        List<ProjectItemImageResponseDto> imageDtos = images.stream()
                .map(image -> new ProjectItemImageResponseDto(
                        image.getId(),
                        image.getImageKey(),
                        resolveUrl(urls, image.getImageKey()),
                        image.getSortOrder()
                ))
                .toList();

        return toAdminDetailResponse(item, imageDtos, urls);
    }

    @Transactional
    public List<ProjectItemImageResponseDto> addImages(Long itemId, AdminItemImageCreateRequestDto request) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));

        List<AdminItemImageCreateRequestDto.ImageRequestDto> images = request.images();
        if (images == null || images.isEmpty()) {
            throw new ItemException(ItemErrorType.IMAGE_REQUIRED);
        }

        Set<Integer> sortOrders = new HashSet<>();
        for (AdminItemImageCreateRequestDto.ImageRequestDto image : images) {
            if (!sortOrders.add(image.sortOrder())) {
                throw new ItemException(ItemErrorType.SORT_ORDER_INVALID, "duplicate sortOrder: " + image.sortOrder());
            }
            if (image.sortOrder() < 0) {
                throw new ItemException(ItemErrorType.SORT_ORDER_INVALID, "sortOrder must be >= 0");
            }
        }

        Set<Integer> existingOrders = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId).stream()
                .map(ItemImage::getSortOrder)
                .collect(Collectors.toSet());
        for (Integer order : sortOrders) {
            if (existingOrders.contains(order)) {
                throw new ItemException(ItemErrorType.SORT_ORDER_CONFLICT, "sortOrder already exists: " + order);
            }
        }

        List<ItemImage> entities = images.stream()
                .map(image -> new ItemImage(item, image.imageKey(), image.sortOrder()))
                .toList();

        List<ItemImage> saved = itemImageRepository.saveAll(entities);
        Set<String> keySet = new LinkedHashSet<>();
        for (ItemImage image : saved) {
            addIfValidKey(keySet, image.getImageKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        return saved.stream()
                .map(image -> new ProjectItemImageResponseDto(
                        image.getId(),
                        image.getImageKey(),
                        resolveUrl(urls, image.getImageKey()),
                        image.getSortOrder()
                ))
                .sorted(Comparator.comparingInt(ProjectItemImageResponseDto::sortOrder))
                .toList();
    }

    @Transactional
    public AdminItemImageOrderPatchResponseDto patchImageOrder(
            Long itemId,
            AdminItemImageOrderPatchRequestDto request
    ) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));

        List<Long> imageIds = request.imageIds();
        if (imageIds == null || imageIds.isEmpty()) {
            throw new ItemException(ItemErrorType.IMAGE_REQUIRED, "imageIds are required");
        }

        Set<Long> uniqueIds = new HashSet<>();
        for (Long id : imageIds) {
            if (!uniqueIds.add(id)) {
                throw new ItemException(ItemErrorType.SORT_ORDER_INVALID, "duplicate imageId: " + id);
            }
        }

        long totalCount = itemImageRepository.countByItemId(itemId);
        if (totalCount != imageIds.size()) {
            throw new ItemException(ItemErrorType.IMAGE_SIZE_MISMATCH);
        }

        List<ItemImage> images = itemImageRepository.findAllById(imageIds);
        if (images.size() != imageIds.size()) {
            throw new ItemException(ItemErrorType.IMAGE_NOT_FOUND);
        }

        Map<Long, ItemImage> imageMap = images.stream()
                .collect(Collectors.toMap(ItemImage::getId, Function.identity()));

        int sortOrder = 0;
        for (Long imageId : imageIds) {
            ItemImage image = imageMap.get(imageId);
            if (!image.getItem().getId().equals(item.getId())) {
                throw new ItemException(ItemErrorType.IMAGE_NOT_BELONG_TO_ITEM, "imageId does not belong to item: " + imageId);
            }
            image.updateSortOrder(sortOrder++);
        }

        return new AdminItemImageOrderPatchResponseDto(itemId, imageIds.size());
    }

    @Transactional
    public void deleteImage(Long itemId, Long imageId) {
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ItemException(ItemErrorType.IMAGE_NOT_FOUND));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ItemException(ItemErrorType.IMAGE_NOT_BELONG_TO_ITEM);
        }
        itemImageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public ProjectItemJournalPresignGetResponseDto createJournalPresignGet(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
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

    private NormalizedItemRequest normalizeCreate(Project project, AdminProjectItemCreateRequestDto request) {
        ItemType itemType = resolveItemType(project, request.itemType(), null);
        validateDescription(itemType, request.description());
        return normalize(
                itemType,
                request.price(),
                request.saleType(),
                request.thumbnailKey(),
                request.targetQty(),
                request.fundedQty(),
                request.journalFileKey(),
                request.stockQty()
        );
    }

    private NormalizedItemRequest normalizeUpdate(
            Project project,
            ProjectItem item,
            AdminProjectItemUpdateRequestDto request
    ) {
        ItemType itemType = resolveItemType(project, request.itemType(), item.getItemType());
        validateDescription(itemType, request.description());
        return normalize(
                itemType,
                request.price(),
                request.saleType(),
                request.thumbnailKey(),
                request.targetQty(),
                request.fundedQty(),
                request.journalFileKey(),
                request.stockQty()
        );
    }

    private NormalizedItemRequest normalize(
            ItemType itemType,
            int price,
            ItemSaleType saleType,
            String thumbnailKey,
            Integer targetQty,
            Integer fundedQty,
            String journalFileKey,
            Integer stockQty
    ) {
        if (itemType == ItemType.DIGITAL_JOURNAL) {
            if (price != 0) {
                throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "price must be 0 for DIGITAL_JOURNAL");
            }
            if (saleType != ItemSaleType.NORMAL) {
                throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "saleType must be NORMAL for DIGITAL_JOURNAL");
            }
            if (targetQty != null) {
                throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "targetQty must be null for DIGITAL_JOURNAL");
            }
            if (fundedQty != null && fundedQty != 0) {
                throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "fundedQty must be null or 0 for DIGITAL_JOURNAL");
            }
            String normalizedJournalKey = toNonBlankString(journalFileKey);
            if (normalizedJournalKey == null) {
                throw new ItemException(ItemErrorType.JOURNAL_FILE_KEY_REQUIRED, "journalFileKey is required for DIGITAL_JOURNAL");
            }
            String normalizedThumbnailKey = toNonBlankString(thumbnailKey);
            return new NormalizedItemRequest(itemType, null, fundedQty, normalizedJournalKey, normalizedThumbnailKey, null);
        }

        String normalizedThumbnailKey = toNonBlankString(thumbnailKey);
        if (normalizedThumbnailKey == null) {
            throw new ItemException(ItemErrorType.THUMBNAIL_REQUIRED);
        }

        if (toNonBlankString(journalFileKey) != null) {
            throw new ItemException(ItemErrorType.DIGITAL_JOURNAL_VIOLATION, "journalFileKey is allowed only for DIGITAL_JOURNAL");
        }

        if (fundedQty != null && fundedQty < 0) {
            throw new ItemException(ItemErrorType.GROUPBUY_VIOLATION, "fundedQty must be >= 0");
        }

        Integer normalizedTargetQty = targetQty;
        Integer normalizedStockQty;
        if (saleType == ItemSaleType.GROUPBUY) {
            if (normalizedTargetQty == null || normalizedTargetQty < 1) {
                throw new ItemException(ItemErrorType.GROUPBUY_VIOLATION, "targetQty must be >= 1 for GROUPBUY");
            }
            normalizedStockQty = null;
        } else {
            normalizedTargetQty = null;
            if (stockQty == null) {
                throw new ItemException(ItemErrorType.NORMAL_SALE_VIOLATION, "stockQty is required for NORMAL");
            }
            if (stockQty < 0) {
                throw new ItemException(ItemErrorType.NORMAL_SALE_VIOLATION, "stockQty must be >= 0");
            }
            normalizedStockQty = stockQty;
        }

        return new NormalizedItemRequest(
                itemType,
                normalizedTargetQty,
                fundedQty,
                null,
                normalizedThumbnailKey,
                normalizedStockQty
        );
    }

    private void validateDescription(ItemType itemType, String description) {
        if (itemType != ItemType.DIGITAL_JOURNAL && toNonBlankString(description) == null) {
            throw new ItemException(ItemErrorType.DESCRIPTION_REQUIRED);
        }
    }

    private ItemType resolveItemType(Project project, ItemType requested, ItemType fallback) {
        ProjectCategory category = project.getCategory() == null ? ProjectCategory.GOODS : project.getCategory();
        ItemType resolved = requested != null ? requested : fallback;
        if (resolved == null) {
            resolved = category == ProjectCategory.JOURNAL ? ItemType.DIGITAL_JOURNAL : ItemType.PHYSICAL;
        }
        if (category == ProjectCategory.JOURNAL && resolved != ItemType.DIGITAL_JOURNAL) {
            throw new ItemException(ItemErrorType.ITEM_TYPE_MISMATCH, "itemType must be DIGITAL_JOURNAL for JOURNAL project");
        }
        if (category == ProjectCategory.GOODS && resolved != ItemType.PHYSICAL) {
            throw new ItemException(ItemErrorType.ITEM_TYPE_MISMATCH, "itemType must be PHYSICAL for GOODS project");
        }
        return resolved;
    }

    private AdminProjectItemResponseDto toAdminResponse(ProjectItem item, Map<String, String> urls) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        Integer stockQty = item.getSaleType() == ItemSaleType.NORMAL ? item.getStockQty() : null;
        return new AdminProjectItemResponseDto(
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
                item.getJournalFileKey(),
                stockQty,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private AdminProjectItemDetailResponseDto toAdminDetailResponse(
            ProjectItem item,
            List<ProjectItemImageResponseDto> images,
            Map<String, String> urls
    ) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        Integer stockQty = item.getSaleType() == ItemSaleType.NORMAL ? item.getStockQty() : null;
        return new AdminProjectItemDetailResponseDto(
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
                item.getJournalFileKey(),
                stockQty,
                images,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private Map<String, String> presignGetForItem(ProjectItem item) {
        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, item.getThumbnailKey());
        return presignGetSafely(keySet);
    }

    private void ensureItemExists(Long itemId) {
        if (!projectItemRepository.existsById(itemId)) {
            throw new ItemException(ItemErrorType.ITEM_NOT_FOUND);
        }
    }

    private AdminItemPresignPutBatchResponseDto toBatchPresignResponse(
            S3PresignFacade.PresignPutBatchResult response
    ) {
        List<S3PresignFacade.PresignPutItem> items = response.items();
        if (items == null || items.isEmpty()) {
            throw new ItemException(ItemErrorType.PRESIGN_FAILED);
        }

        List<AdminItemPresignPutBatchResponseDto.ItemDto> result = new ArrayList<>(items.size());
        for (S3PresignFacade.PresignPutItem item : items) {
            result.add(new AdminItemPresignPutBatchResponseDto.ItemDto(
                    item.fileName(),
                    item.key(),
                    item.uploadUrl(),
                    item.expiresInSeconds()
            ));
        }

        return new AdminItemPresignPutBatchResponseDto(result);
    }

    private List<S3PresignFacade.PresignPutFile> toPresignFiles(
            List<AdminItemPresignPutBatchRequestDto.FileDto> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new ItemException(ItemErrorType.FILE_REQUIRED);
        }

        List<S3PresignFacade.PresignPutFile> result = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            AdminItemPresignPutBatchRequestDto.FileDto file = files.get(i);
            if (file == null) {
                throw new ItemException(ItemErrorType.FILE_REQUIRED, "files[" + i + "] is null");
            }
            result.add(new S3PresignFacade.PresignPutFile(
                    file.fileName(),
                    file.contentType()
            ));
        }

        return result;
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

    private record NormalizedItemRequest(
            ItemType itemType,
            Integer targetQty,
            Integer fundedQty,
            String journalFileKey,
            String thumbnailKey,
            Integer stockQty
    ) {
    }

    private record GroupbuyInfo(
            Integer targetQty,
            Integer fundedQty,
            Double achievementRate,
            Integer remainingQty
    ) {
    }
}
