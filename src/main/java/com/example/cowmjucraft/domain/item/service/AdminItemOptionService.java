package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionGroupResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionValueResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.exception.ItemErrorType;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemOptionRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminItemOptionService {

    private final ProjectItemRepository projectItemRepository;
    private final ItemOptionGroupRepository itemOptionGroupRepository;
    private final ItemOptionValueRepository itemOptionValueRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    @Transactional(readOnly = true)
    public List<AdminItemOptionGroupResponseDto> getOptionGroups(Long itemId) {
        findItem(itemId);

        List<ItemOptionGroup> groups = itemOptionGroupRepository.findByItemIdOrderBySortOrderAsc(itemId);
        List<Long> groupIds = groups.stream().map(ItemOptionGroup::getId).toList();
        Map<Long, List<ItemOptionValue>> valuesByGroupId = itemOptionValueRepository
                .findByOptionGroupIdInOrderBySortOrderAsc(groupIds).stream()
                .collect(Collectors.groupingBy(value -> value.getOptionGroup().getId()));

        return groups.stream()
                .map(group -> toGroupResponse(group, valuesByGroupId.getOrDefault(group.getId(), List.of())))
                .toList();
    }

    @Transactional
    public AdminItemOptionGroupResponseDto createOptionGroup(Long itemId, AdminItemOptionGroupCreateRequestDto request) {
        ProjectItem item = findItem(itemId);
        if (item.getSaleType() != ItemSaleType.NORMAL) {
            // 공동구매 상품은 재고를 fundedQty/targetQty로 별도 관리하므로 옵션(옵션값 단위 재고)과 병행하지 않는다.
            throw new ItemException(ItemErrorType.OPTION_NOT_SUPPORTED_FOR_SALE_TYPE, "itemId=" + itemId);
        }
        if (itemOptionGroupRepository.existsByItemIdAndSortOrder(itemId, request.sortOrder())) {
            throw new ItemException(ItemErrorType.SORT_ORDER_CONFLICT, "itemId=" + itemId);
        }

        ItemOptionGroup group = itemOptionGroupRepository.save(
                new ItemOptionGroup(item, request.name(), request.required(), request.sortOrder())
        );
        return toGroupResponse(group, List.of());
    }

    @Transactional
    public AdminItemOptionGroupResponseDto updateOptionGroup(
            Long itemId,
            Long groupId,
            AdminItemOptionGroupUpdateRequestDto request
    ) {
        ItemOptionGroup group = findGroup(itemId, groupId);
        if (request.sortOrder() != group.getSortOrder()
                && itemOptionGroupRepository.existsByItemIdAndSortOrder(itemId, request.sortOrder())) {
            throw new ItemException(ItemErrorType.SORT_ORDER_CONFLICT, "itemId=" + itemId);
        }

        group.update(request.name(), request.required(), request.sortOrder());
        List<ItemOptionValue> values = itemOptionValueRepository.findByOptionGroupIdOrderBySortOrderAsc(groupId);
        return toGroupResponse(group, values);
    }

    @Transactional
    public void deleteOptionGroup(Long itemId, Long groupId) {
        ItemOptionGroup group = findGroup(itemId, groupId);
        List<Long> valueIds = itemOptionValueRepository.findByOptionGroupIdOrderBySortOrderAsc(groupId).stream()
                .map(ItemOptionValue::getId)
                .toList();
        // DB CASCADE로 하위 값이 같이 삭제되므로, 그중 하나라도 주문에 쓰였으면 여기서 먼저 막는다
        // (안 그러면 order_item_options의 FK 제약에 걸려 원인 파악이 어려운 DB 예외로 실패한다).
        if (!valueIds.isEmpty() && orderItemOptionRepository.existsByOptionValueIdIn(valueIds)) {
            throw new ItemException(ItemErrorType.OPTION_VALUE_IN_USE, "groupId=" + groupId);
        }
        itemOptionGroupRepository.delete(group);
    }

    @Transactional
    public AdminItemOptionValueResponseDto createOptionValue(
            Long itemId,
            Long groupId,
            AdminItemOptionValueCreateRequestDto request
    ) {
        ItemOptionGroup group = findGroup(itemId, groupId);
        if (itemOptionValueRepository.existsByOptionGroupIdAndSortOrder(groupId, request.sortOrder())) {
            throw new ItemException(ItemErrorType.SORT_ORDER_CONFLICT, "groupId=" + groupId);
        }

        ItemOptionValue value = itemOptionValueRepository.save(
                new ItemOptionValue(group, request.name(), request.additionalPrice(), request.stockQty(), request.sortOrder())
        );
        return toValueResponse(value);
    }

    @Transactional
    public AdminItemOptionValueResponseDto updateOptionValue(
            Long itemId,
            Long groupId,
            Long valueId,
            AdminItemOptionValueUpdateRequestDto request
    ) {
        ItemOptionGroup group = findGroup(itemId, groupId);
        ItemOptionValue value = findValue(group, valueId);
        if (request.sortOrder() != value.getSortOrder()
                && itemOptionValueRepository.existsByOptionGroupIdAndSortOrder(groupId, request.sortOrder())) {
            throw new ItemException(ItemErrorType.SORT_ORDER_CONFLICT, "groupId=" + groupId);
        }

        value.update(request.name(), request.additionalPrice(), request.stockQty(), request.sortOrder());
        return toValueResponse(value);
    }

    @Transactional
    public void deleteOptionValue(Long itemId, Long groupId, Long valueId) {
        ItemOptionGroup group = findGroup(itemId, groupId);
        ItemOptionValue value = findValue(group, valueId);
        if (orderItemOptionRepository.existsByOptionValueId(valueId)) {
            throw new ItemException(ItemErrorType.OPTION_VALUE_IN_USE, "valueId=" + valueId);
        }
        itemOptionValueRepository.delete(value);
    }

    private ProjectItem findItem(Long itemId) {
        return projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ItemErrorType.ITEM_NOT_FOUND));
    }

    private ItemOptionGroup findGroup(Long itemId, Long groupId) {
        ItemOptionGroup group = itemOptionGroupRepository.findById(groupId)
                .orElseThrow(() -> new ItemException(ItemErrorType.OPTION_GROUP_NOT_FOUND));
        if (!group.getItem().getId().equals(itemId)) {
            throw new ItemException(ItemErrorType.OPTION_GROUP_NOT_BELONG_TO_ITEM, "itemId=" + itemId + ", groupId=" + groupId);
        }
        return group;
    }

    private ItemOptionValue findValue(ItemOptionGroup group, Long valueId) {
        ItemOptionValue value = itemOptionValueRepository.findById(valueId)
                .orElseThrow(() -> new ItemException(ItemErrorType.OPTION_VALUE_NOT_FOUND));
        if (!value.getOptionGroup().getId().equals(group.getId())) {
            throw new ItemException(
                    ItemErrorType.OPTION_VALUE_NOT_BELONG_TO_GROUP,
                    "groupId=" + group.getId() + ", valueId=" + valueId
            );
        }
        return value;
    }

    private AdminItemOptionGroupResponseDto toGroupResponse(ItemOptionGroup group, List<ItemOptionValue> values) {
        return new AdminItemOptionGroupResponseDto(
                group.getId(),
                group.getItem().getId(),
                group.getName(),
                group.isRequired(),
                group.getSortOrder(),
                values.stream().map(this::toValueResponse).toList()
        );
    }

    private AdminItemOptionValueResponseDto toValueResponse(ItemOptionValue value) {
        return new AdminItemOptionValueResponseDto(
                value.getId(),
                value.getOptionGroup().getId(),
                value.getName(),
                value.getAdditionalPrice(),
                value.getStockQty(),
                value.getSortOrder()
        );
    }
}
