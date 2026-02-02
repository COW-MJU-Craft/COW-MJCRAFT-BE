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
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.domain.project.entity.Project;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));

        NormalizedItemRequest normalized = normalizeCreate(request);
        ProjectItem item = new ProjectItem(
                project,
                request.name(),
                request.description(),
                request.price(),
                request.saleType(),
                request.status(),
                request.thumbnailKey(),
                normalized.targetQty(),
                normalized.fundedQty()
        );
        ProjectItem saved = projectItemRepository.save(item);
        Map<String, String> urls = presignGetForItem(saved);
        return toAdminResponse(saved, urls);
    }

    @Transactional
    public AdminProjectItemResponseDto update(Long itemId, AdminProjectItemUpdateRequestDto request) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));

        NormalizedItemRequest normalized = normalizeUpdate(request);
        item.update(
                request.name(),
                request.description(),
                request.price(),
                request.saleType(),
                request.status(),
                request.thumbnailKey(),
                normalized.targetQty(),
                normalized.fundedQty()
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

    @Transactional
    public void delete(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));
        projectItemRepository.delete(item);
    }

    @Transactional
    public void deleteThumbnail(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));

        String key = toNonBlankString(item.getThumbnailKey());
        if (key != null) {
            try {
                s3PresignFacade.deleteByKeys(List.of(key));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 삭제 실패");
            }
        }

        item.clearThumbnail();
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));

        List<AdminItemImageCreateRequestDto.ImageRequestDto> images = request.images();
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "images are required");
        }

        Set<Integer> sortOrders = new HashSet<>();
        for (AdminItemImageCreateRequestDto.ImageRequestDto image : images) {
            if (!sortOrders.add(image.sortOrder())) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "duplicate sortOrder: " + image.sortOrder()
                );
            }
            if (image.sortOrder() < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "sortOrder must be >= 0");
            }
        }

        Set<Integer> existingOrders = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId).stream()
                .map(ItemImage::getSortOrder)
                .collect(Collectors.toSet());
        for (Integer order : sortOrders) {
            if (existingOrders.contains(order)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "sortOrder already exists: " + order);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));

        List<Long> imageIds = request.imageIds();
        if (imageIds == null || imageIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "imageIds are required");
        }

        Set<Long> uniqueIds = new HashSet<>();
        for (Long id : imageIds) {
            if (!uniqueIds.add(id)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "duplicate imageId: " + id);
            }
        }

        long totalCount = itemImageRepository.countByItemId(itemId);
        if (totalCount != imageIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "imageIds size must match item images"
            );
        }

        List<ItemImage> images = itemImageRepository.findAllById(imageIds);
        if (images.size() != imageIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "some imageIds not found");
        }

        Map<Long, ItemImage> imageMap = images.stream()
                .collect(Collectors.toMap(ItemImage::getId, Function.identity()));

        int sortOrder = 0;
        for (Long imageId : imageIds) {
            ItemImage image = imageMap.get(imageId);
            if (!image.getItem().getId().equals(item.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "imageId does not belong to item: " + imageId
                );
            }
            image.updateSortOrder(sortOrder++);
        }

        return new AdminItemImageOrderPatchResponseDto(itemId, imageIds.size());
    }

    @Transactional
    public void deleteImage(Long itemId, Long imageId) {
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item image not found"));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "imageId does not belong to item");
        }
        itemImageRepository.delete(image);
    }

    private NormalizedItemRequest normalizeCreate(AdminProjectItemCreateRequestDto request) {
        return normalize(
                request.saleType(),
                request.targetQty(),
                request.fundedQty()
        );
    }

    private NormalizedItemRequest normalizeUpdate(AdminProjectItemUpdateRequestDto request) {
        return normalize(
                request.saleType(),
                request.targetQty(),
                request.fundedQty()
        );
    }

    private NormalizedItemRequest normalize(
            ItemSaleType saleType,
            Integer targetQty,
            Integer fundedQty
    ) {
        int normalizedFundedQty = fundedQty == null ? 0 : fundedQty;
        if (normalizedFundedQty < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "fundedQty must be >= 0");
        }

        Integer normalizedTargetQty = targetQty;
        if (saleType == ItemSaleType.GROUPBUY) {
            if (normalizedTargetQty == null || normalizedTargetQty < 1) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "targetQty must be >= 1 for GROUPBUY"
                );
            }
        } else {
            normalizedTargetQty = null;
        }

        return new NormalizedItemRequest(normalizedTargetQty, normalizedFundedQty);
    }

    private AdminProjectItemResponseDto toAdminResponse(ProjectItem item, Map<String, String> urls) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        return new AdminProjectItemResponseDto(
                item.getId(),
                item.getProject().getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getSaleType(),
                item.getStatus(),
                item.getThumbnailKey(),
                resolveUrl(urls, item.getThumbnailKey()),
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
        return new AdminProjectItemDetailResponseDto(
                item.getId(),
                item.getProject().getId(),
                item.getName(),
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found");
        }
    }

    private AdminItemPresignPutBatchResponseDto toBatchPresignResponse(
            S3PresignFacade.PresignPutBatchResult response
    ) {
        List<S3PresignFacade.PresignPutItem> items = response.items();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("presign items is empty");
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
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "files are required");
        }

        List<S3PresignFacade.PresignPutFile> result = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            AdminItemPresignPutBatchRequestDto.FileDto file = files.get(i);
            if (file == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "files[" + i + "] is null"
                );
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

    private record NormalizedItemRequest(Integer targetQty, int fundedQty) {
    }

    private record GroupbuyInfo(
            Integer targetQty,
            Integer fundedQty,
            Double achievementRate,
            Integer remainingQty
    ) {
    }
}
