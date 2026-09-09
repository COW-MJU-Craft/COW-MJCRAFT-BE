package com.example.cowmjucraft.domain.order.controller.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.service.OrderCompletePageService;
import com.example.cowmjucraft.domain.order.service.OrderCreateService;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import com.example.cowmjucraft.domain.order.service.OrderLookupIdService;
import com.example.cowmjucraft.domain.order.service.OrderQueryByTokenService;
import com.example.cowmjucraft.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClientOrderControllerViewTokenTest {

    private static final String HEADER = "X-Order-View-Token";

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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClientOrderController(
                        orderCreateService,
                        orderLookupIdService,
                        orderDetailQueryService,
                        orderQueryByTokenService,
                        orderCompletePageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void viewOrderByToken_헤더로전달_헤더토큰으로조회한다() throws Exception {
        // given
        given(orderQueryByTokenService.getOrderDetailByToken("header-token")).willReturn(null);

        // when
        mockMvc.perform(get("/api/orders/view").header(HEADER, "header-token"))
                .andExpect(status().isOk());

        // then
        verify(orderQueryByTokenService).getOrderDetailByToken("header-token");
    }

    @Test
    void viewOrderByToken_쿼리파라미터로전달_구버전링크도동작한다() throws Exception {
        // given
        given(orderQueryByTokenService.getOrderDetailByToken("query-token")).willReturn(null);

        // when
        mockMvc.perform(get("/api/orders/view").param("token", "query-token"))
                .andExpect(status().isOk());

        // then
        verify(orderQueryByTokenService).getOrderDetailByToken("query-token");
    }

    @Test
    void viewOrderByToken_헤더와쿼리동시전달_헤더가우선한다() throws Exception {
        // given
        given(orderQueryByTokenService.getOrderDetailByToken("header-token")).willReturn(null);

        // when
        mockMvc.perform(get("/api/orders/view")
                        .header(HEADER, "header-token")
                        .param("token", "query-token"))
                .andExpect(status().isOk());

        // then
        verify(orderQueryByTokenService).getOrderDetailByToken("header-token");
    }

    @Test
    void viewOrderByToken_토큰누락_400을반환한다() throws Exception {
        // given & when
        mockMvc.perform(get("/api/orders/view"))
                .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(orderQueryByTokenService);
    }

    @Test
    void viewOrderByToken_헤더가공백_400을반환한다() throws Exception {
        // given & when
        mockMvc.perform(get("/api/orders/view").header(HEADER, "   "))
                .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(orderQueryByTokenService);
    }

    @Test
    void getOrderCompletePage_헤더로전달_헤더토큰으로조회한다() throws Exception {
        // given
        given(orderCompletePageService.getOrderCompletePage("header-token")).willReturn(null);

        // when
        mockMvc.perform(get("/api/orders/complete-page").header(HEADER, "header-token"))
                .andExpect(status().isOk());

        // then
        verify(orderCompletePageService).getOrderCompletePage("header-token");
    }

    @Test
    void getOrderCompletePage_토큰누락_400을반환한다() throws Exception {
        // given & when
        mockMvc.perform(get("/api/orders/complete-page"))
                .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(orderCompletePageService);
    }
}
