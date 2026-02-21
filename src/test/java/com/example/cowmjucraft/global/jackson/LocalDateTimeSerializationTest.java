package com.example.cowmjucraft.global.jackson;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class LocalDateTimeSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void LocalDateTime이_ISO8601_문자열로_직렬화된다() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 2, 20, 14, 30, 0);

        String json = objectMapper.writeValueAsString(dateTime);

        assertThat(json).isEqualTo("\"2026-02-20T14:30:00\"");
    }

    @Test
    void LocalDateTime이_숫자_배열이_아닌_문자열로_직렬화된다() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 2, 20, 14, 30, 0);

        String json = objectMapper.writeValueAsString(dateTime);

        assertThat(json).startsWith("\"");
        assertThat(json).contains("T");
        assertThat(json).doesNotStartWith("[");
    }
}
