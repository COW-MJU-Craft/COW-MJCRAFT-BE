package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderCompletePageUpsertRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderCompletePageResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderCompletePage;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderCompletePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderCompletePageService {

    private final OrderCompletePageRepository orderCompletePageRepository;

    @Transactional(readOnly = true)
    public AdminOrderCompletePageResponseDto getOrderCompletePage() {
        OrderCompletePage orderCompletePage = orderCompletePageRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_COMPLETE_PAGE_NOT_FOUND));

        return toResponse(orderCompletePage);
    }

    @Transactional
    public AdminOrderCompletePageResponseDto upsertOrderCompletePage(
            AdminOrderCompletePageUpsertRequestDto request
    ) {
        String messageTitle = normalizeRequiredText(request.messageTitle(), "메시지 제목");
        String messageDescription = trimToNull(request.messageDescription());
        String paymentInformation = normalizeRequiredText(request.paymentInformation(), "결제 정보");

        OrderCompletePage orderCompletePage = orderCompletePageRepository.findFirstByOrderByIdAsc()
                .orElse(null);

        if (orderCompletePage == null) {
            orderCompletePage = orderCompletePageRepository.save(
                    new OrderCompletePage(
                            messageTitle,
                            messageDescription,
                            paymentInformation
                    )
            );
        } else {
            orderCompletePage.update(
                    messageTitle,
                    messageDescription,
                    paymentInformation
            );
        }

        return toResponse(orderCompletePage);
    }

    private AdminOrderCompletePageResponseDto toResponse(OrderCompletePage orderCompletePage) {
        return new AdminOrderCompletePageResponseDto(
                orderCompletePage.getId(),
                orderCompletePage.getMessageTitle(),
                orderCompletePage.getMessageDescription(),
                orderCompletePage.getPaymentInformation()
        );
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new OrderException(
                    OrderErrorType.REQUIRED_FIELD_MISSING,
                    fieldName + "은(는) 필수입니다."
            );
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}