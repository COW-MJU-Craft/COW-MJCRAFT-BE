package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ItemImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findByItemIdOrderBySortOrderAsc(Long itemId);

    List<ItemImage> findByItemIdIn(List<Long> itemIds);

    void deleteByItemIdIn(List<Long> itemIds);

    long countByItemId(Long itemId);
}
