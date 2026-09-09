package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPricingService {

    private final ProjectItemRepository projectItemRepository;
    private final OrderPolicyRepository orderPolicyRepository;

    @Transactional(readOnly = true)
    public PriceQuote calculate(
            List<OrderCreateItemRequestDto> items,
            OrderFulfillmentMethod fulfillmentMethod
    ) {
        Map<Long, Integer> quantityByItemId = aggregateItemQuantities(items);
        Map<Long, ProjectItem> projectItemById = findProjectItems(quantityByItemId.keySet());
        int totalAmount = 0;
        List<PriceLine> lines = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : quantityByItemId.entrySet()) {
            Long projectItemId = entry.getKey();
            int quantity = entry.getValue();
            ProjectItem projectItem = projectItemById.get(projectItemId);
            if (projectItem == null) {
                throw new OrderException(OrderErrorType.ITEM_NOT_FOUND, "projectItemId=" + projectItemId);
            }

            validateOrderable(projectItem, quantity);

            int unitPrice = projectItem.getPrice();
            int lineAmount;
            try {
                lineAmount = Math.multiplyExact(unitPrice, quantity);
                totalAmount = Math.addExact(totalAmount, lineAmount);
            } catch (ArithmeticException exception) {
                throw new OrderException(OrderErrorType.ORDER_AMOUNT_OVERFLOW);
            }
            lines.add(new PriceLine(projectItem, quantity, unitPrice, lineAmount));
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

    private Map<Long, Integer> aggregateItemQuantities(List<OrderCreateItemRequestDto> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderException(OrderErrorType.ORDER_ITEMS_REQUIRED);
        }

        Map<Long, Integer> quantityByItemId = new LinkedHashMap<>();
        for (OrderCreateItemRequestDto item : items) {
            if (item == null || item.projectItemId() == null) {
                throw new OrderException(OrderErrorType.INVALID_ORDER_ITEM);
            }
            if (item.quantity() <= 0) {
                throw new OrderException(OrderErrorType.QUANTITY_MUST_BE_POSITIVE);
            }
            try {
                quantityByItemId.merge(item.projectItemId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new OrderException(OrderErrorType.ORDER_AMOUNT_OVERFLOW);
            }
        }
        return quantityByItemId;
    }

    /** 상품 종류만큼 select이 나가지 않도록 한 번에 조회한다. 순서는 호출부의 요청 순서를 그대로 따른다. */
    private Map<Long, ProjectItem> findProjectItems(Collection<Long> projectItemIds) {
        return projectItemRepository.findAllById(projectItemIds).stream()
                .collect(Collectors.toMap(ProjectItem::getId, Function.identity()));
    }

    private void validateOrderable(ProjectItem projectItem, int quantity) {
        if (projectItem.getStatus() != ItemStatus.OPEN) {
            throw new OrderException(OrderErrorType.ITEM_NOT_AVAILABLE, "projectItemId=" + projectItem.getId());
        }
        if (projectItem.getSaleType() == ItemSaleType.NORMAL) {
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

    private int getDefaultShippingFee() {
        OrderPolicy orderPolicy = orderPolicyRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_POLICY_NOT_FOUND));
        return orderPolicy.getDefaultShippingFee();
    }

    public record PriceQuote(List<PriceLine> lines, int totalAmount, int shippingFee, int finalAmount) {
    }

    public record PriceLine(ProjectItem projectItem, int quantity, int unitPrice, int lineAmount) {
    }
}
