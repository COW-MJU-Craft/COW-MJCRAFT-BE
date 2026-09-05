package com.example.cowmjucraft.domain.order.controller.admin;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderExportResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.service.AdminOrderExportService;
import com.example.cowmjucraft.global.exception.GlobalExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminOrderExportControllerTest {

    @Mock
    private AdminOrderExportService adminOrderExportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrderExportController(adminOrderExportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void exportProjectOrders_필터입력_CSV다운로드응답반환() throws Exception {
        // given
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 5);
        byte[] csv = "CSV 내용".getBytes(StandardCharsets.UTF_8);
        given(adminOrderExportService.exportProjectOrders(
                1L,
                startDate,
                endDate,
                OrderStatus.PAID,
                OrderFulfillmentMethod.DELIVERY
        )).willReturn(new AdminOrderExportResponseDto("가을 프로젝트_주문목록.csv", csv));

        // when & then
        mockMvc.perform(get("/api/admin/projects/1/orders/export")
                        .queryParam("startDate", "2026-09-01")
                        .queryParam("endDate", "2026-09-05")
                        .queryParam("status", "PAID")
                        .queryParam("fulfillmentMethod", "DELIVERY"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(csv))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        verify(adminOrderExportService).exportProjectOrders(
                1L,
                startDate,
                endDate,
                OrderStatus.PAID,
                OrderFulfillmentMethod.DELIVERY
        );
    }

    @Test
    void exportOrdersByDate_날짜입력_CSV다운로드응답반환() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 9, 5);
        byte[] csv = "CSV 내용".getBytes(StandardCharsets.UTF_8);
        given(adminOrderExportService.exportOrdersByDate(date, date, null, null))
                .willReturn(new AdminOrderExportResponseDto("주문목록_20260905-20260905.csv", csv));

        // when & then
        mockMvc.perform(get("/api/admin/orders/export")
                        .queryParam("startDate", "2026-09-05")
                        .queryParam("endDate", "2026-09-05"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(csv));
        verify(adminOrderExportService).exportOrdersByDate(date, date, null, null);
    }

    @Test
    void exportOrdersByDate_종료일누락_400반환() throws Exception {
        // given
        given(adminOrderExportService.exportOrdersByDate(
                LocalDate.of(2026, 9, 5),
                null,
                null,
                null
        )).willThrow(new OrderException(OrderErrorType.INVALID_EXPORT_DATE_RANGE));

        // when & then
        mockMvc.perform(get("/api/admin/orders/export")
                        .queryParam("startDate", "2026-09-05"))
                .andExpect(status().isBadRequest());
    }
}
