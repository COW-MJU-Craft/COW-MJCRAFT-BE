package com.example.cowmjucraft.domain.payout.repository;


import com.example.cowmjucraft.domain.payout.entity.PayoutItem;
import com.example.cowmjucraft.domain.payout.entity.PayoutItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutItemRepository extends JpaRepository<PayoutItem, Long> {

    List<PayoutItem> findAllByPayoutId(Long payoutId);

    List<PayoutItem> findAllByPayoutIdAndType(Long payoutId, PayoutItemType type);
}
