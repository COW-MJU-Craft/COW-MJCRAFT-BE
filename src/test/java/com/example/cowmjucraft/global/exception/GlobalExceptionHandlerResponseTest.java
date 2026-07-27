package com.example.cowmjucraft.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerResponseTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 도메인예외_detail의내부식별자가응답에노출되지않는다() throws Exception {
        mockMvc.perform(get("/test/domain-exception"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(OrderErrorType.INSUFFICIENT_STOCK.getMessage()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("projectItemId"))));
    }

    @Test
    void 필수값누락_필드명안내는응답에유지된다() throws Exception {
        mockMvc.perform(get("/test/required-field"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입금자명은(는) 필수입니다."));
    }

    @Test
    void IllegalArgumentException_예외메시지가응답에노출되지않는다() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.not(Matchers.containsString("internal detail"))));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/domain-exception")
        void domainException() {
            throw new OrderException(OrderErrorType.INSUFFICIENT_STOCK, "projectItemId=42");
        }

        @GetMapping("/test/required-field")
        void requiredField() {
            throw OrderException.requiredField(OrderErrorType.REQUIRED_FIELD_MISSING, "입금자명");
        }

        @GetMapping("/test/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("internal detail leaked");
        }
    }
}
