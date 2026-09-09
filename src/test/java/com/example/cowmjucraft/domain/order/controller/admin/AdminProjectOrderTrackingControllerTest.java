package com.example.cowmjucraft.domain.order.controller.admin;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderTrackingInformationResponseDto;
import com.example.cowmjucraft.domain.order.service.AdminOrderTrackingInformationService;
import com.example.cowmjucraft.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminProjectOrderTrackingControllerTest {

    @Mock
    private AdminOrderTrackingInformationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminProjectOrderTrackingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateTrackingInformation_운송장정보입력_수정결과반환() throws Exception {
        // given
        given(service.updateTrackingInformation(1L, 10L, "CJ대한통운 1234"))
                .willReturn(new AdminOrderTrackingInformationResponseDto(10L, "CJ대한통운 1234"));

        // when & then
        mockMvc.perform(put("/api/admin/projects/1/orders/10/tracking-information")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trackingInformation":"CJ대한통운 1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingInformation").value("CJ대한통운 1234"));
    }
}
