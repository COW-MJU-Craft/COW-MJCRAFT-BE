package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemOptionValueRepository extends JpaRepository<ItemOptionValue, Long> {

    List<ItemOptionValue> findByOptionGroupIdOrderBySortOrderAsc(Long optionGroupId);

    List<ItemOptionValue> findByOptionGroupIdInOrderBySortOrderAsc(List<Long> optionGroupIds);

    boolean existsByOptionGroupIdAndSortOrder(Long optionGroupId, int sortOrder);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ItemOptionValue v where v.id = :id")
    Optional<ItemOptionValue> findByIdForUpdate(@Param("id") Long id);
}
