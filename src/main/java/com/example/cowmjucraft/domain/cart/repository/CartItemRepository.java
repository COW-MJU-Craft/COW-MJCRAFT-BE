package com.example.cowmjucraft.domain.cart.repository;

import com.example.cowmjucraft.domain.cart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {"item"})
    List<CartItem> findByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);

    @EntityGraph(attributePaths = {"item"})
    Optional<CartItem> findByIdAndMemberId(Long id, Long memberId);

    Optional<CartItem> findByMemberIdAndItemId(Long memberId, Long itemId);

    void deleteByMemberId(Long memberId);
}
