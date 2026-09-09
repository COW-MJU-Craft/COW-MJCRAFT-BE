package com.example.cowmjucraft.domain.order.controller.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.dto.request.OrderQuoteRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderQuoteResponseDto;
import com.example.cowmjucraft.domain.order.service.OrderCompletePageService;
import com.example.cowmjucraft.domain.order.service.OrderCreateService;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import com.example.cowmjucraft.domain.order.service.OrderLookupIdService;
import com.example.cowmjucraft.domain.order.service.OrderQueryByTokenService;
import com.example.cowmjucraft.domain.order.service.OrderQuoteService;
import com.example.cowmjucraft.global.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClientOrderQuoteControllerTest {

    @Mock
    private OrderCreateService orderCreateService;
    @Mock
    private OrderLookupIdService orderLookupIdService;
    @Mock
    private OrderDetailQueryService orderDetailQueryService;
    @Mock
    private OrderQueryByTokenService orderQueryByTokenService;
    @Mock
    private OrderCompletePageService orderCompletePageService;
    @Mock
    private OrderQuoteService orderQuoteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ClientOrderController controller = new ClientOrderController(
                orderCreateService,
                orderLookupIdService,
                orderDetailQueryService,
                orderQueryByTokenService,
                orderCompletePageService,
                orderQuoteService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void quoteOrder_상품과수령방식으로현재금액반환() throws Exception {
        // given
        given(orderQuoteService.quote(any(OrderQuoteRequestDto.class)))
                .willReturn(new OrderQuoteResponseDto(
                        List.of(new OrderQuoteResponseDto.ItemDto(1L, 10L, "키링", 2, 3_000, 6_000)),
                        6_000,
                        3_500,
                        9_500
                ));

        // when & then
        mockMvc.perform(post("/api/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"projectItemId": 1, "quantity": 2}],
                                  "fulfillmentMethod": "DELIVERY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(6000))
                .andExpect(jsonPath("$.data.shippingFee").value(3500))
                .andExpect(jsonPath("$.data.finalAmount").value(9500));

        verify(orderQuoteService).quote(any(OrderQuoteRequestDto.class));
    }
}
