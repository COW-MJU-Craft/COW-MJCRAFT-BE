package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemOptionValueRepository extends JpaRepository<ItemOptionValue, Long> {

    List<ItemOptionValue> findByOptionGroupIdOrderBySortOrderAsc(Long optionGroupId);

    List<ItemOptionValue> findByOptionGroupIdInOrderBySortOrderAsc(List<Long> optionGroupIds);

    boolean existsByOptionGroupIdAndSortOrder(Long optionGroupId, int sortOrder);
}
