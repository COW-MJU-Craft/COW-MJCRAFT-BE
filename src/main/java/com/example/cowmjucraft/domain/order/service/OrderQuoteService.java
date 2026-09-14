package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.request.OrderQuoteRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderQuoteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQuoteService {

    private final OrderPricingService orderPricingService;

    @Transactional(readOnly = true)
    public OrderQuoteResponseDto quote(OrderQuoteRequestDto request) {
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                request.items(),
                request.fulfillmentMethod()
        );
        return new OrderQuoteResponseDto(
                quote.lines().stream()
                        .map(line -> new OrderQuoteResponseDto.ItemDto(
                                line.projectItem().getId(),
                                line.projectItem().getProject().getId(),
                                line.projectItem().getName(),
                                line.quantity(),
                                line.unitPrice(),
                                line.lineAmount()
                        ))
                        .toList(),
                quote.totalAmount(),
                quote.shippingFee(),
                quote.finalAmount()
        );
    }
}
