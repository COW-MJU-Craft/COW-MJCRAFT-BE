package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayoutSummaryResponse {

    private long totalIncome;

    private long totalExpense;

    private long netProfit;

    private double profitRate;
}
