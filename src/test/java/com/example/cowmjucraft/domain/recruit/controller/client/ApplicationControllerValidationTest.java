package com.example.cowmjucraft.domain.recruit.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ApplicationCreateRequest/UpdateRequest는 이번에 처음 {@code @Valid}가 배선되므로,
 * DTO 단위 검증만으로는 컨트롤러가 실제로 검증을 트리거하는지 증명할 수 없다.
 * 실제 HTTP 요청으로 확인한다 — 이 프로젝트에 아직 @WebMvcTest 선례가 없어
 * 이미 쓰는 @SpringBootTest에 @AutoConfigureMockMvc만 얹는 방식을 쓴다.
 * /api/application은 SecurityConfig에서 permitAll이라 인증 목킹이 필요 없다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerValidationTest {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createApplication_답변101개_검증실패로_422() throws Exception {
        String body = requestBody(oversizedAnswers());

        mockMvc.perform(post("/api/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(422));
    }

    @Test
    void createApplication_답변3개_검증통과후_도메인로직도달() throws Exception {
        String body = requestBody(normalAnswers());

        // 검증을 통과해 서비스까지 도달하면, 폼이 없어 RECRUITMENT_CLOSED(409)로 응답한다.
        // (422 VALIDATION_FAILED가 아니라는 것 자체가 @Size가 정상 범위를 막지 않는다는 증거)
        mockMvc.perform(post("/api/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("모집이 마감되었습니다."));
    }

    @Test
    void updateApplication_답변101개_검증실패로_422() throws Exception {
        String body = requestBody(oversizedAnswers());

        mockMvc.perform(put("/api/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(422));
    }

    @Test
    void updateApplication_답변3개_검증통과후_도메인로직도달() throws Exception {
        String body = requestBody(normalAnswers());

        mockMvc.perform(put("/api/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("모집이 마감되었습니다."));
    }

    private String requestBody(List<Map<String, Object>> answers) {
        return JSON_MAPPER.writeValueAsString(Map.of(
                "studentId", "60123456",
                "password", "password1234",
                "answers", answers
        ));
    }

    private List<Map<String, Object>> oversizedAnswers() {
        List<Map<String, Object>> answers = new ArrayList<>();
        for (long i = 0; i < 101; i++) {
            answers.add(Map.of("formQuestionId", i, "value", "답변"));
        }
        return answers;
    }

    private List<Map<String, Object>> normalAnswers() {
        return List.of(
                Map.of("formQuestionId", 1L, "value", "답변1"),
                Map.of("formQuestionId", 2L, "value", "답변2"),
                Map.of("formQuestionId", 3L, "value", "답변3")
        );
    }
}
