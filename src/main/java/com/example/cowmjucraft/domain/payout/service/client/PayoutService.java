package com.example.cowmjucraft.domain.payout.service.client;

import com.example.cowmjucraft.domain.payout.dto.response.*;
import com.example.cowmjucraft.domain.payout.entity.Payout;
import com.example.cowmjucraft.domain.payout.entity.PayoutItem;
import com.example.cowmjucraft.domain.payout.entity.PayoutItemType;
import com.example.cowmjucraft.domain.payout.exception.PayoutErrorType;
import com.example.cowmjucraft.domain.payout.exception.PayoutException;
import com.example.cowmjucraft.domain.payout.repository.PayoutRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final ProjectRepository projectRepository;

    public PayoutListWrapperResponse getPayoutList() {
        List<PayoutListResponse> payoutListResponses = payoutRepository.findAll().stream().map(this::convertToPayoutListResponse).toList();

        return new PayoutListWrapperResponse(payoutListResponses);
    }

    public PayoutDetailResponse getPayoutDetail(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId).orElseThrow(() -> new PayoutException(PayoutErrorType.PAYOUT_NOT_FOUND));

        List<PayoutIncomeItemResponse> incomeItemResponses =
                payout.getItems().stream()
                        .filter(payoutItem -> payoutItem.getType() == PayoutItemType.INCOME)
                        .map(payoutItem -> new PayoutIncomeItemResponse(
                                payoutItem.getId(),
                                payoutItem.getName(),
                                payoutItem.getAmount()
                        ))
                        .toList();

        List<PayoutItem> expenseItems =
                payout.getItems().stream()
                        .filter(payoutItem -> payoutItem.getType() == PayoutItemType.EXPENSE)
                        .toList();

        Map<String, List<PayoutItem>> groupedExpenseItems =
                expenseItems.stream()
                        .collect(Collectors.groupingBy(
                                payoutItem -> payoutItem.getCategory() == null ? "기타" : payoutItem.getCategory(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<PayoutExpenseCategoryGroupResponse> expenseCategoryGroupResponses = new ArrayList<>();

        for (Map.Entry<String, List<PayoutItem>> entry : groupedExpenseItems.entrySet()) {

            String category = entry.getKey();
            List<PayoutItem> categoryItems = entry.getValue();

            long categoryTotalAmount =
                    categoryItems.stream()
                            .mapToLong(PayoutItem::getAmount)
                            .sum();

            List<PayoutExpenseItemResponse> expenseItemResponses =
                    categoryItems.stream()
                            .map(payoutItem -> new PayoutExpenseItemResponse(
                                    payoutItem.getId(),
                                    payoutItem.getName(),
                                    payoutItem.getAmount()
                            ))
                            .toList();

            expenseCategoryGroupResponses.add(
                    new PayoutExpenseCategoryGroupResponse(
                            category,
                            categoryTotalAmount,
                            expenseItemResponses
                    )
            );
        }

        PayoutSummaryResponse payoutSummaryResponse =
                new PayoutSummaryResponse(
                        payout.getTotalIncome(),
                        payout.getTotalExpense(),
                        payout.getNetProfit(),
                        payout.getProfitRate()
                );

        return new PayoutDetailResponse(
                payout.getId(),
                payout.getTitle(),
                payout.getProject().getId(),
                payout.getSemester(),
                payoutSummaryResponse,
                incomeItemResponses,
                expenseCategoryGroupResponses
        );
    }

    public PayoutDetailResponse getPayoutDetailByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new PayoutException(PayoutErrorType.PROJECT_NOT_FOUND));

        if (project.getStatus() != ProjectStatus.CLOSED) {
            throw new PayoutException(PayoutErrorType.PROJECT_NOT_CLOSED);
        }

        Payout payout = payoutRepository.findByProjectId(projectId).orElseThrow(() -> new PayoutException(PayoutErrorType.PROJECT_NOT_FOUND));

        List<PayoutIncomeItemResponse> incomeItemResponses =
                payout.getItems().stream()
                        .filter(payoutItem -> payoutItem.getType() == PayoutItemType.INCOME)
                        .map(payoutItem -> new PayoutIncomeItemResponse(
                                payoutItem.getId(),
                                payoutItem.getName(),
                                payoutItem.getAmount()
                        ))
                        .toList();

        List<PayoutItem> expenseItems =
                payout.getItems().stream()
                        .filter(payoutItem -> payoutItem.getType() == PayoutItemType.EXPENSE)
                        .toList();

        Map<String, List<PayoutItem>> groupedExpenseItems =
                expenseItems.stream()
                        .collect(Collectors.groupingBy(
                                payoutItem -> payoutItem.getCategory() == null ? "기타" : payoutItem.getCategory(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<PayoutExpenseCategoryGroupResponse> expenseCategoryGroupResponses = new ArrayList<>();

        for (Map.Entry<String, List<PayoutItem>> entry : groupedExpenseItems.entrySet()) {

            String category = entry.getKey();
            List<PayoutItem> categoryItems = entry.getValue();

            long categoryTotalAmount =
                    categoryItems.stream()
                            .mapToLong(PayoutItem::getAmount)
                            .sum();

            List<PayoutExpenseItemResponse> expenseItemResponses =
                    categoryItems.stream()
                            .map(payoutItem -> new PayoutExpenseItemResponse(
                                    payoutItem.getId(),
                                    payoutItem.getName(),
                                    payoutItem.getAmount()
                            ))
                            .toList();

            expenseCategoryGroupResponses.add(
                    new PayoutExpenseCategoryGroupResponse(
                            category,
                            categoryTotalAmount,
                            expenseItemResponses
                    )
            );
        }

        PayoutSummaryResponse payoutSummaryResponse =
                new PayoutSummaryResponse(
                        payout.getTotalIncome(),
                        payout.getTotalExpense(),
                        payout.getNetProfit(),
                        payout.getProfitRate()
                );

        return new PayoutDetailResponse(
                payout.getId(),
                payout.getTitle(),
                payout.getProject().getId(),
                payout.getSemester(),
                payoutSummaryResponse,
                incomeItemResponses,
                expenseCategoryGroupResponses
        );
    }

    private PayoutListResponse convertToPayoutListResponse(Payout payout) {

        PayoutSummaryResponse payoutSummaryResponse =
                new PayoutSummaryResponse(
                        payout.getTotalIncome(),
                        payout.getTotalExpense(),
                        payout.getNetProfit(),
                        payout.getProfitRate()
                );

        return new PayoutListResponse(
                payout.getId(),
                payout.getTitle(),
                payout.getSemester(),
                payoutSummaryResponse
        );
    }
}
