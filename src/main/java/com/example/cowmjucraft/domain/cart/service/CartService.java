package com.example.cowmjucraft.domain.cart.service;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.accounts.user.repository.MemberRepository;
import com.example.cowmjucraft.domain.cart.dto.request.CartItemCreateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.request.CartItemQuantityUpdateRequestDto;
import com.example.cowmjucraft.domain.cart.dto.response.CartItemResponseDto;
import com.example.cowmjucraft.domain.cart.dto.response.CartSummaryResponseDto;
import com.example.cowmjucraft.domain.cart.entity.CartItem;
import com.example.cowmjucraft.domain.cart.repository.CartItemRepository;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
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
public class CartService {

    private final MemberRepository memberRepository;
    private final ProjectItemRepository projectItemRepository;
    private final CartItemRepository cartItemRepository;
    private final S3PresignFacade s3PresignFacade;

    @Transactional(readOnly = true)
    public CartSummaryResponseDto getCart(Long memberId) {
        getMember(memberId);

        List<CartItem> cartItems = cartItemRepository.findByMemberIdOrderByCreatedAtDescIdDesc(memberId);
        Set<String> thumbnailKeys = new LinkedHashSet<>();
        for (CartItem cartItem : cartItems) {
            addIfValidKey(thumbnailKeys, cartItem.getItem().getThumbnailKey());
        }
        Map<String, String> urls = presignGetSafely(thumbnailKeys);

        List<CartItemResponseDto> items = new ArrayList<>();
        int totalQuantity = 0;
        int totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            ProjectItem item = cartItem.getItem();
            int linePrice = item.getPrice() * cartItem.getQuantity();
            totalQuantity += cartItem.getQuantity();
            totalPrice += linePrice;

            items.add(new CartItemResponseDto(
                    cartItem.getId(),
                    item.getId(),
                    item.getName(),
                    item.getSaleType(),
                    item.getStatus(),
                    resolveUrl(urls, item.getThumbnailKey()),
                    item.getPrice(),
                    cartItem.getQuantity(),
                    linePrice
            ));
        }

        return new CartSummaryResponseDto(items.size(), totalQuantity, totalPrice, items);
    }

    @Transactional
    public CartSummaryResponseDto addCartItem(Long memberId, CartItemCreateRequestDto request) {
        Member member = getMember(memberId);
        ProjectItem item = getItem(request.itemId());
        validatePurchasable(item, request.quantity());

        CartItem cartItem = cartItemRepository.findByMemberIdAndItemId(memberId, item.getId())
                .orElseGet(() -> new CartItem(member, item, 0));

        cartItem.increaseQuantity(request.quantity());
        cartItemRepository.save(cartItem);

        return getCart(memberId);
    }

    @Transactional
    public CartSummaryResponseDto updateCartItemQuantity(
            Long memberId,
            Long cartItemId,
            CartItemQuantityUpdateRequestDto request
    ) {
        CartItem cartItem = cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "cart item not found"));

        validatePurchasable(cartItem.getItem(), request.quantity());
        cartItem.changeQuantity(request.quantity());

        return getCart(memberId);
    }

    @Transactional
    public void removeCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long memberId) {
        getMember(memberId);
        cartItemRepository.deleteByMemberId(memberId);
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
        String normalized = toNonBlankString(key);
        return normalized == null ? null : urls.get(normalized);
    }

    private void addIfValidKey(Set<String> keys, String value) {
        String normalized = toNonBlankString(value);
        if (normalized != null) {
            keys.add(normalized);
        }
    }

    private String toNonBlankString(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
