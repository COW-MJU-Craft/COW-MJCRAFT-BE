package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.accounts.user.repository.MemberRepository;
import com.example.cowmjucraft.domain.cart.entity.CartItem;
import com.example.cowmjucraft.domain.cart.repository.CartItemRepository;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.BuyNowRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.CartCheckoutRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.PurchaseOrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.PurchaseOrderLineResponseDto;
import com.example.cowmjucraft.domain.order.entity.PurchaseOrder;
import com.example.cowmjucraft.domain.order.entity.PurchaseOrderItem;
import com.example.cowmjucraft.domain.order.entity.PurchaseOrderType;
import com.example.cowmjucraft.domain.order.repository.PurchaseOrderRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class PurchaseOrderService {

    private final MemberRepository memberRepository;
    private final ProjectItemRepository projectItemRepository;
    private final CartItemRepository cartItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional
    public PurchaseOrderCreateResponseDto buyNow(Long memberId, BuyNowRequestDto request) {
        Member member = getMember(memberId);
        ProjectItem item = getItem(request.itemId());
        validatePurchasable(item, request.quantity());

        PurchaseOrder order = new PurchaseOrder(member, PurchaseOrderType.BUY_NOW);
        addOrderLine(order, item, request.quantity());
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderCreateResponseDto checkoutCart(Long memberId, CartCheckoutRequestDto request) {
        Member member = getMember(memberId);
        List<CartItem> selectedCartItems = findCartItems(memberId, request.cartItemIds());
        if (selectedCartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no cart items selected");
        }

        PurchaseOrder order = new PurchaseOrder(member, PurchaseOrderType.CART);
        for (CartItem cartItem : selectedCartItems) {
            addOrderLine(order, cartItem.getItem(), cartItem.getQuantity());
        }

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        cartItemRepository.deleteAll(selectedCartItems);

        return toResponse(saved);
    }

    private List<CartItem> findCartItems(Long memberId, List<Long> cartItemIds) {
        Set<Long> deduplicatedIds = new LinkedHashSet<>(cartItemIds);
        List<CartItem> selectedCartItems = new ArrayList<>();
        for (Long cartItemId : deduplicatedIds) {
            CartItem cartItem = cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "cart item not found: " + cartItemId));
            selectedCartItems.add(cartItem);
        }
        return selectedCartItems;
    }

    private void addOrderLine(PurchaseOrder order, ProjectItem item, int quantity) {
        validatePurchasable(item, quantity);

        PurchaseOrderItem orderItem = new PurchaseOrderItem(order, item, quantity);
        order.addItem(orderItem);

        if (item.getSaleType() == ItemSaleType.GROUPBUY) {
            item.increaseFundedQty(quantity);
        }
    }

    private PurchaseOrderCreateResponseDto toResponse(PurchaseOrder order) {
        List<PurchaseOrderLineResponseDto> lines = order.getItems().stream()
                .map(item -> new PurchaseOrderLineResponseDto(
                        item.getItem().getId(),
                        item.getItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLinePrice()
                ))
                .toList();

        return new PurchaseOrderCreateResponseDto(
                order.getId(),
                order.getOrderType(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                lines
        );
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member not found"));
    }

    private ProjectItem getItem(Long itemId) {
        return projectItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found"));
    }

    private void validatePurchasable(ProjectItem item, int quantity) {
        if (item.getStatus() != ItemStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "item is not open for purchase");
        }

        if (item.getSaleType() == ItemSaleType.GROUPBUY && item.getTargetQty() != null) {
            int remaining = Math.max(item.getTargetQty() - item.getFundedQty(), 0);
            if (remaining < quantity) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "requested quantity exceeds remaining groupbuy quantity");
            }
        }
    }
}
