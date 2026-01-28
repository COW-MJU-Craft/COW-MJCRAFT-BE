package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemImageOrderPatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.util.HashSet;
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

    @Transactional
    public AdminProjectItemResponseDto create(Long projectId, AdminProjectItemCreateRequestDto request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("project not found"));

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
        return toAdminResponse(saved);
    }

    @Transactional
    public AdminProjectItemResponseDto update(Long itemId, AdminProjectItemUpdateRequestDto request) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("item not found"));

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
        return toAdminResponse(item);
    }

    @Transactional
    public void delete(Long itemId) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("item not found"));
        projectItemRepository.delete(item);
    }

    @Transactional
    public List<ProjectItemImageResponseDto> addImages(Long itemId, AdminItemImageCreateRequestDto request) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("item not found"));

        List<AdminItemImageCreateRequestDto.ImageRequestDto> images = request.images();
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("images are required");
        }

        Set<Integer> sortOrders = new HashSet<>();
        for (AdminItemImageCreateRequestDto.ImageRequestDto image : images) {
            if (!sortOrders.add(image.sortOrder())) {
                throw new IllegalArgumentException("duplicate sortOrder: " + image.sortOrder());
            }
            if (image.sortOrder() < 0) {
                throw new IllegalArgumentException("sortOrder must be >= 0");
            }
        }

        Set<Integer> existingOrders = itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId).stream()
                .map(ItemImage::getSortOrder)
                .collect(Collectors.toSet());
        for (Integer order : sortOrders) {
            if (existingOrders.contains(order)) {
                throw new IllegalArgumentException("sortOrder already exists: " + order);
            }
        }

        List<ItemImage> entities = images.stream()
                .map(image -> new ItemImage(item, image.imageKey(), image.sortOrder()))
                .toList();

        return itemImageRepository.saveAll(entities).stream()
                .map(ProjectItemImageResponseDto::from)
                .toList();
    }

    @Transactional
    public AdminItemImageOrderPatchResponseDto patchImageOrder(
            Long itemId,
            AdminItemImageOrderPatchRequestDto request
    ) {
        ProjectItem item = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("item not found"));

        List<Long> imageIds = request.imageIds();
        if (imageIds == null || imageIds.isEmpty()) {
            throw new IllegalArgumentException("imageIds are required");
        }

        Set<Long> uniqueIds = new HashSet<>();
        for (Long id : imageIds) {
            if (!uniqueIds.add(id)) {
                throw new IllegalArgumentException("duplicate imageId: " + id);
            }
        }

        long totalCount = itemImageRepository.countByItemId(itemId);
        if (totalCount != imageIds.size()) {
            throw new IllegalArgumentException("imageIds size must match item images");
        }

        List<ItemImage> images = itemImageRepository.findAllById(imageIds);
        if (images.size() != imageIds.size()) {
            throw new IllegalArgumentException("some imageIds not found");
        }

        Map<Long, ItemImage> imageMap = images.stream()
                .collect(Collectors.toMap(ItemImage::getId, Function.identity()));

        int sortOrder = 0;
        for (Long imageId : imageIds) {
            ItemImage image = imageMap.get(imageId);
            if (!image.getItem().getId().equals(item.getId())) {
                throw new IllegalArgumentException("imageId does not belong to item: " + imageId);
            }
            image.updateSortOrder(sortOrder++);
        }

        return new AdminItemImageOrderPatchResponseDto(itemId, imageIds.size());
    }

    @Transactional
    public void deleteImage(Long itemId, Long imageId) {
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("item image not found"));
        if (!image.getItem().getId().equals(itemId)) {
            throw new IllegalArgumentException("imageId does not belong to item");
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
            throw new IllegalArgumentException("fundedQty must be >= 0");
        }

        Integer normalizedTargetQty = targetQty;
        if (saleType == ItemSaleType.GROUPBUY) {
            if (normalizedTargetQty == null || normalizedTargetQty < 1) {
                throw new IllegalArgumentException("targetQty must be >= 1 for GROUPBUY");
            }
        } else {
            normalizedTargetQty = null;
        }

        return new NormalizedItemRequest(normalizedTargetQty, normalizedFundedQty);
    }

    private AdminProjectItemResponseDto toAdminResponse(ProjectItem item) {
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
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
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
