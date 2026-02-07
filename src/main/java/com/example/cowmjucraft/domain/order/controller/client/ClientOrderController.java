package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.service.OrderCreateService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ClientOrderController implements ClientOrderControllerDocs {

    private final OrderCreateService orderCreateService;

    @PostMapping("/orders")
    @Override
    public ApiResult<OrderCreateResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, orderCreateService.createOrder(request));
    }
}
