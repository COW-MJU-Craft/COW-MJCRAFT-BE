package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayoutExpenseItemResponse {

    private Long payoutItemId;

    private String name;

    private long amount;
}
