package com.example.cowmjucraft.domain.order.service;

import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderQuoteRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderQuoteResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderQuoteServiceTest {

    @Mock
    private OrderPricingService orderPricingService;

    @Test
    void quote_가격계산결과를클라이언트응답으로변환한다() {
        // given
        OrderQuoteService orderQuoteService = new OrderQuoteService(orderPricingService);
        ProjectItem projectItem = projectItem();
        OrderPricingService.PriceQuote priceQuote = new OrderPricingService.PriceQuote(
                List.of(new OrderPricingService.PriceLine(projectItem, 2, 3_000, 6_000)),
                6_000,
                3_500,
                9_500
        );
        OrderQuoteRequestDto request = new OrderQuoteRequestDto(
                List.of(new OrderCreateItemRequestDto(1L, 2)),
                OrderFulfillmentMethod.DELIVERY
        );
        given(orderPricingService.calculate(anyList(), eq(OrderFulfillmentMethod.DELIVERY)))
                .willReturn(priceQuote);

        // when
        OrderQuoteResponseDto response = orderQuoteService.quote(request);

        // then
        assertThat(response.totalAmount()).isEqualTo(6_000);
        assertThat(response.shippingFee()).isEqualTo(3_500);
        assertThat(response.finalAmount()).isEqualTo(9_500);
        assertThat(response.items()).containsExactly(
                new OrderQuoteResponseDto.ItemDto(1L, 10L, "키링", 2, 3_000, 6_000)
        );
        verify(orderPricingService).calculate(request.items(), OrderFulfillmentMethod.DELIVERY);
    }

    private ProjectItem projectItem() {
        ProjectItem item = new ProjectItem(
                project(10L),
                "키링",
                "summary",
                "description",
                3_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                null,
                null,
                null,
                null,
                10
        );
        ReflectionTestUtils.setField(item, "id", 1L);
        return item;
    }
}
