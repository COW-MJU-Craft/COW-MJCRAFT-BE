package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PayoutDetailResponse {

    private Long payoutId;

    private String title;

    private Long projectId;

    private String semester;

    private PayoutSummaryResponse summary;

    private List<PayoutIncomeItemResponse> incomes;

    private List<PayoutExpenseCategoryGroupResponse> expenseGroups;
}
