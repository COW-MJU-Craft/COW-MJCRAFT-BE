package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryByTokenService {

    private final OrderDetailQueryService orderDetailQueryService;

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderDetailByToken(String token) {
        return orderDetailQueryService.getByViewToken(token);
    }
}
