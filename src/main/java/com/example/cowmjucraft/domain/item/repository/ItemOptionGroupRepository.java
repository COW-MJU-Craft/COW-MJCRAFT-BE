package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemOptionGroupRepository extends JpaRepository<ItemOptionGroup, Long> {

    List<ItemOptionGroup> findByItemIdOrderBySortOrderAsc(Long itemId);

    List<ItemOptionGroup> findByItemIdInOrderBySortOrderAsc(List<Long> itemIds);

    boolean existsByItemId(Long itemId);

    boolean existsByItemIdAndSortOrder(Long itemId, int sortOrder);
}
