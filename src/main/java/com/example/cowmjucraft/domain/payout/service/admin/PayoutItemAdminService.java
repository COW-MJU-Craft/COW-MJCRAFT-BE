package com.example.cowmjucraft.domain.payout.service.admin;

import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutItemUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.entity.Payout;
import com.example.cowmjucraft.domain.payout.entity.PayoutItem;
import com.example.cowmjucraft.domain.payout.exception.PayoutErrorType;
import com.example.cowmjucraft.domain.payout.exception.PayoutException;
import com.example.cowmjucraft.domain.payout.repository.PayoutItemRepository;
import com.example.cowmjucraft.domain.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayoutItemAdminService {

    private final PayoutRepository payoutRepository;
    private final PayoutItemRepository payoutItemRepository;

    @Transactional
    public Long createPayoutItem(Long payoutId, PayoutItemCreateAdminRequest payoutItemCreateAdminRequest) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new PayoutException(PayoutErrorType.PAYOUT_NOT_FOUND));

        PayoutItem payoutItem = new PayoutItem(
                payoutItemCreateAdminRequest.getType(),
                payoutItemCreateAdminRequest.getName().trim(),
                payoutItemCreateAdminRequest.getAmount(),
                payoutItemCreateAdminRequest.getCategory()
        );

        payout.addItem(payoutItem);
        payout.calculateSummary();

        payoutRepository.save(payout);
        return payoutItem.getId();
    }

    @Transactional
    public void updatePayoutItem(
            Long payoutId,
            Long payoutItemId,
            PayoutItemUpdateAdminRequest payoutItemUpdateAdminRequest
    ) {
        PayoutItem payoutItem = payoutItemRepository.findById(payoutItemId)
                .orElseThrow(() -> new PayoutException(PayoutErrorType.PAYOUT_NOT_FOUND));

        if (!payoutItem.getPayout().getId().equals(payoutId)) {
            throw new PayoutException(PayoutErrorType.PAYOUT_ITEM_NOT_BELONG_TO_PAYOUT);
        }

        payoutItem.update(
                payoutItemUpdateAdminRequest.getType(),
                payoutItemUpdateAdminRequest.getName().trim(),
                payoutItemUpdateAdminRequest.getAmount(),
                payoutItemUpdateAdminRequest.getCategory()
        );

        payoutItem.getPayout().calculateSummary();
    }

    @Transactional
    public void deletePayoutItem(Long payoutId, Long payoutItemId) {
        PayoutItem payoutItem = payoutItemRepository.findById(payoutItemId)
                .orElseThrow(() -> new PayoutException(PayoutErrorType.PAYOUT_ITEM_NOT_FOUND));

        if (!payoutItem.getPayout().getId().equals(payoutId)) {
            throw new PayoutException(PayoutErrorType.PAYOUT_ITEM_NOT_BELONG_TO_PAYOUT);
        }

        Payout payout = payoutItem.getPayout();
        payout.removeItem(payoutItem);
        payout.calculateSummary();
    }
}