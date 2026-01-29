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
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<ProjectItemListResponseDto> getItems(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));

        List<ProjectItem> items = projectItemRepository.findByProjectIdOrderByCreatedAtDescIdDesc(project.getId());
        return items.stream()
                .map(this::toListResponse)
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

        return toDetailResponse(item, images);
    }

    private ProjectItemListResponseDto toListResponse(ProjectItem item) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        return new ProjectItemListResponseDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSaleType(),
                item.getStatus(),
                item.getThumbnailKey(),
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty()
        );
    }

    private ProjectItemDetailResponseDto toDetailResponse(
            ProjectItem item,
            List<ProjectItemImageResponseDto> images
    ) {
        GroupbuyInfo info = calculateGroupbuyInfo(item);
        return new ProjectItemDetailResponseDto(
                item.getId(),
                item.getProject().getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getSaleType(),
                item.getStatus(),
                item.getThumbnailKey(),
                images,
                info.targetQty(),
                info.fundedQty(),
                info.achievementRate(),
                info.remainingQty()
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

    private record GroupbuyInfo(
            Integer targetQty,
            Integer fundedQty,
            Double achievementRate,
            Integer remainingQty
    ) {
    }
}
