package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.OrderLookupIdAvailabilityResponseDto;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLookupIdService {

    private final OrderAuthRepository orderAuthRepository;

    @Transactional(readOnly = true)
    public OrderLookupIdAvailabilityResponseDto checkAvailability(String lookupId) {
        String normalizedLookupId = normalizeRequiredText(lookupId, "조회 아이디");
        boolean available = !orderAuthRepository.existsByLookupId(normalizedLookupId);
        return new OrderLookupIdAvailabilityResponseDto(normalizedLookupId, available);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new OrderException(OrderErrorType.LOOKUP_FIELD_REQUIRED, fieldName + "은(는) 필수입니다.");
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
