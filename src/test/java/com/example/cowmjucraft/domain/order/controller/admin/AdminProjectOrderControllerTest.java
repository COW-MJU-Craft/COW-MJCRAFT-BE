package com.example.cowmjucraft.domain.order.controller.admin;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderBulkAdvanceResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminProjectOrderStatisticsResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.service.AdminProjectOrderService;
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
class AdminProjectOrderControllerTest {

    @Mock
    private AdminProjectOrderService adminProjectOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminProjectOrderController(adminProjectOrderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getOrders_프로젝트와상태필터_서비스결과반환() throws Exception {
        // given
        given(adminProjectOrderService.getOrders(1L, OrderStatus.IN_PRODUCTION)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/admin/projects/1/orders")
                        .queryParam("status", "IN_PRODUCTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        verify(adminProjectOrderService).getOrders(1L, OrderStatus.IN_PRODUCTION);
    }

    @Test
    void getStatistics_프로젝트집계반환() throws Exception {
        // given
        given(adminProjectOrderService.getStatistics(1L))
                .willReturn(new AdminProjectOrderStatisticsResponseDto(2L, 35000L));

        // when & then
        mockMvc.perform(get("/api/admin/projects/1/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderCount").value(2))
                .andExpect(jsonPath("$.data.totalOrderAmount").value(35000));
    }

    @Test
    void advanceOrderStatus_개별주문진행_다음상태반환() throws Exception {
        // given
        given(adminProjectOrderService.advanceOrderStatus(1L, 10L))
                .willReturn(new AdminOrderStatusResponseDto(10L, "READY_TO_SHIP"));

        // when & then
        mockMvc.perform(post("/api/admin/projects/1/orders/10/advance-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(10))
                .andExpect(jsonPath("$.data.status").value("READY_TO_SHIP"));
    }

    @Test
    void advanceOrderStatuses_주문목록입력_일괄변경결과반환() throws Exception {
        // given
        given(adminProjectOrderService.advanceOrderStatuses(1L, List.of(10L, 20L)))
                .willReturn(new AdminOrderBulkAdvanceResponseDto(
                        "IN_PRODUCTION",
                        "READY_TO_SHIP",
                        List.of(10L, 20L)
                ));

        // when & then
        mockMvc.perform(post("/api/admin/projects/1/orders/advance-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderIds":[10,20]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.previousStatus").value("IN_PRODUCTION"))
                .andExpect(jsonPath("$.data.newStatus").value("READY_TO_SHIP"));
    }
}
