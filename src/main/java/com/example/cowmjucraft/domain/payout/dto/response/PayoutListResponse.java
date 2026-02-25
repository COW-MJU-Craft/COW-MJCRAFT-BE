package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayoutListResponse {

    private Long payoutId;

    private String title;

    private String semester;

    private PayoutSummaryResponse summary;
}
