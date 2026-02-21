package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PayoutExpenseCategoryGroupResponse {

    private String category;

    private long categoryTotalAmount;

    private List<PayoutExpenseItemResponse> items;
}
