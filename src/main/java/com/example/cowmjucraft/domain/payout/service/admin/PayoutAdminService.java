package com.example.cowmjucraft.domain.payout.service.admin;

import com.example.cowmjucraft.domain.payout.dto.request.PayoutCreateAdminRequest;
import com.example.cowmjucraft.domain.payout.dto.request.PayoutUpdateAdminRequest;
import com.example.cowmjucraft.domain.payout.entity.Payout;
import com.example.cowmjucraft.domain.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayoutAdminService {

    private final PayoutRepository payoutRepository;

    @Transactional
    public Long createPayout(PayoutCreateAdminRequest payoutCreateAdminRequest) {
        Payout payout = new Payout(
                payoutCreateAdminRequest.getTitle().trim(),
                payoutCreateAdminRequest.getSemester().trim()
        );
        payout.calculateSummary();
        payoutRepository.save(payout);
        return payout.getId();
    }

    @Transactional
    public void updatePayout(Long payoutId, PayoutUpdateAdminRequest payoutUpdateAdminRequest) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("PAYOUT_NOT_FOUND"));

        payout.changeTitle(payoutUpdateAdminRequest.getTitle().trim());
        payout.changeSemester(payoutUpdateAdminRequest.getSemester().trim());
    }

    @Transactional
    public void deletePayout(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("PAYOUT_NOT_FOUND"));
        payoutRepository.delete(payout);
    }
}