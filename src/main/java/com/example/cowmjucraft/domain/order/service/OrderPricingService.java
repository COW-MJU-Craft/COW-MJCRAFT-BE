package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPricingService {

    private final ProjectItemRepository projectItemRepository;
    private final ItemOptionGroupRepository itemOptionGroupRepository;
    private final ItemOptionValueRepository itemOptionValueRepository;
    private final OrderPolicyRepository orderPolicyRepository;

    @Transactional(readOnly = true)
    public PriceQuote calculate(
            List<OrderCreateItemRequestDto> items,
            OrderFulfillmentMethod fulfillmentMethod
    ) {
        Map<AggregationKey, Integer> quantityByKey = aggregateItemQuantities(items);

        // Set으로 유지 — projectItemRepository.findAllById(Collection)이 기존에 Set을 받던 것과
        // 호출 형태를 맞춘다(LinkedHashSet이라 이후 List 변환 시 순서도 보존됨).
        Set<Long> projectItemIds = quantityByKey.keySet().stream()
                .map(AggregationKey::projectItemId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ProjectItem> projectItemById = findProjectItems(projectItemIds);
        Map<Long, List<ItemOptionGroup>> optionGroupsByItemId = itemOptionGroupRepository
                .findByItemIdInOrderBySortOrderAsc(List.copyOf(projectItemIds)).stream()
                .collect(Collectors.groupingBy(group -> group.getItem().getId()));
        Map<Long, ItemOptionValue> optionValueById = findOptionValues(quantityByKey.keySet());

        int totalAmount = 0;
        List<PriceLine> lines = new ArrayList<>();

        for (Map.Entry<AggregationKey, Integer> entry : quantityByKey.entrySet()) {
            AggregationKey key = entry.getKey();
            int quantity = entry.getValue();
            ProjectItem projectItem = projectItemById.get(key.projectItemId());
            if (projectItem == null) {
                throw new OrderException(OrderErrorType.ITEM_NOT_FOUND, "projectItemId=" + key.projectItemId());
            }

            List<ItemOptionValue> selectedOptions = resolveSelectedOptions(projectItem, key.optionValueIds(), optionValueById);
            validateRequiredOptions(
                    projectItem,
                    optionGroupsByItemId.getOrDefault(projectItem.getId(), List.of()),
                    selectedOptions
            );
            validateOrderable(projectItem, selectedOptions, quantity);

            int unitPrice;
            int lineAmount;
            try {
                int optionsAdditionalPrice = 0;
                for (ItemOptionValue selectedOption : selectedOptions) {
                    optionsAdditionalPrice = Math.addExact(optionsAdditionalPrice, selectedOption.getAdditionalPrice());
                }
                unitPrice = Math.addExact(projectItem.getPrice(), optionsAdditionalPrice);
                lineAmount = Math.multiplyExact(unitPrice, quantity);
                totalAmount = Math.addExact(totalAmount, lineAmount);
            } catch (ArithmeticException exception) {
                throw new OrderException(OrderErrorType.ORDER_AMOUNT_OVERFLOW);
            }
            lines.add(new PriceLine(projectItem, selectedOptions, quantity, unitPrice, lineAmount));
        }

        int shippingFee = fulfillmentMethod == OrderFulfillmentMethod.DELIVERY
                ? getDefaultShippingFee()
                : 0;
        int finalAmount;
        try {
            finalAmount = Math.addExact(totalAmount, shippingFee);
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorType.ORDER_AMOUNT_OVERFLOW);
        }
        return new PriceQuote(List.copyOf(lines), totalAmount, shippingFee, finalAmount);
    }

    private Map<AggregationKey, Integer> aggregateItemQuantities(List<OrderCreateItemRequestDto> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderException(OrderErrorType.ORDER_ITEMS_REQUIRED);
        }

        Map<AggregationKey, Integer> quantityByKey = new LinkedHashMap<>();
        for (OrderCreateItemRequestDto item : items) {
            if (item == null || item.projectItemId() == null) {
                throw new OrderException(OrderErrorType.INVALID_ORDER_ITEM);
            }
            if (item.quantity() <= 0) {
                throw new OrderException(OrderErrorType.QUANTITY_MUST_BE_POSITIVE);
            }
            AggregationKey key = new AggregationKey(item.projectItemId(), normalizeOptionValueIds(item.optionValueIds()));
            try {
                quantityByKey.merge(key, item.quantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new OrderException(OrderErrorType.ORDER_AMOUNT_OVERFLOW);
            }
        }
        return quantityByKey;
    }

    private List<Long> normalizeOptionValueIds(List<Long> optionValueIds) {
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return List.of();
        }
        return optionValueIds.stream().sorted().toList();
    }

    /** 상품 종류만큼 select이 나가지 않도록 한 번에 조회한다. 순서는 호출부의 요청 순서를 그대로 따른다. */
    private Map<Long, ProjectItem> findProjectItems(Collection<Long> projectItemIds) {
        return projectItemRepository.findAllById(projectItemIds).stream()
                .filter(item -> !item.isArchived())
                .filter(item -> !item.getProject().isArchived())
                .collect(Collectors.toMap(ProjectItem::getId, Function.identity()));
    }

    private Map<Long, ItemOptionValue> findOptionValues(Collection<AggregationKey> keys) {
        Set<Long> optionValueIds = keys.stream()
                .flatMap(key -> key.optionValueIds().stream())
                .collect(Collectors.toSet());
        if (optionValueIds.isEmpty()) {
            return Map.of();
        }
        return itemOptionValueRepository.findAllById(optionValueIds).stream()
                .collect(Collectors.toMap(ItemOptionValue::getId, Function.identity()));
    }

    private List<ItemOptionValue> resolveSelectedOptions(
            ProjectItem projectItem,
            List<Long> optionValueIds,
            Map<Long, ItemOptionValue> optionValueById
    ) {
        if (optionValueIds.isEmpty()) {
            return List.of();
        }

        List<ItemOptionValue> selected = new ArrayList<>();
        Set<Long> seenGroupIds = new HashSet<>();
        for (Long optionValueId : optionValueIds) {
            ItemOptionValue value = optionValueById.get(optionValueId);
            if (value == null || !value.getOptionGroup().getItem().getId().equals(projectItem.getId())) {
                throw new OrderException(
                        OrderErrorType.OPTION_NOT_BELONG_TO_ITEM,
                        "projectItemId=" + projectItem.getId() + ", optionValueId=" + optionValueId
                );
            }
            Long groupId = value.getOptionGroup().getId();
            if (!seenGroupIds.add(groupId)) {
                throw new OrderException(OrderErrorType.DUPLICATE_OPTION_GROUP_SELECTION, "groupId=" + groupId);
            }
            selected.add(value);
        }
        return selected;
    }

    private void validateRequiredOptions(
            ProjectItem projectItem,
            List<ItemOptionGroup> optionGroups,
            List<ItemOptionValue> selectedOptions
    ) {
        Set<Long> selectedGroupIds = selectedOptions.stream()
                .map(value -> value.getOptionGroup().getId())
                .collect(Collectors.toSet());
        for (ItemOptionGroup group : optionGroups) {
            if (group.isRequired() && !selectedGroupIds.contains(group.getId())) {
                throw new OrderException(
                        OrderErrorType.REQUIRED_OPTION_MISSING,
                        "projectItemId=" + projectItem.getId() + ", groupId=" + group.getId()
                );
            }
        }
    }

    private void validateOrderable(ProjectItem projectItem, List<ItemOptionValue> selectedOptions, int quantity) {
        if (projectItem.getStatus() != ItemStatus.OPEN) {
            throw new OrderException(OrderErrorType.ITEM_NOT_AVAILABLE, "projectItemId=" + projectItem.getId());
        }
        if (projectItem.getSaleType() == ItemSaleType.NORMAL) {
            if (!selectedOptions.isEmpty()) {
                validateOptionStock(selectedOptions, quantity);
                return;
            }
            Integer stockQty = projectItem.getStockQty();
            if (stockQty == null || stockQty < quantity) {
                throw new OrderException(OrderErrorType.INSUFFICIENT_STOCK, "projectItemId=" + projectItem.getId());
            }
            return;
        }
        if (projectItem.getSaleType() != ItemSaleType.GROUPBUY) {
            throw new OrderException(OrderErrorType.SALE_TYPE_NOT_ORDERABLE, "projectItemId=" + projectItem.getId());
        }
    }

    private void validateOptionStock(List<ItemOptionValue> selectedOptions, int quantity) {
        for (ItemOptionValue value : selectedOptions) {
            Integer stockQty = value.getStockQty();
            // null = 무제한 재고 — 옵션값은 상품과 달리 재고 미설정이 정상 상태다.
            if (stockQty != null && stockQty < quantity) {
                throw new OrderException(OrderErrorType.INSUFFICIENT_STOCK, "optionValueId=" + value.getId());
            }
        }
    }

    private int getDefaultShippingFee() {
        OrderPolicy orderPolicy = orderPolicyRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_POLICY_NOT_FOUND));
        return orderPolicy.getDefaultShippingFee();
    }

    private record AggregationKey(Long projectItemId, List<Long> optionValueIds) {
    }

    public record PriceQuote(List<PriceLine> lines, int totalAmount, int shippingFee, int finalAmount) {
    }

    public record PriceLine(
            ProjectItem projectItem,
            List<ItemOptionValue> selectedOptions,
            int quantity,
            int unitPrice,
            int lineAmount
    ) {
    }
}
