package com.example.cowmjucraft.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리버스 프록시 뒤에서 실제 클라이언트 IP가 인식되는지 검증한다.
 * IP 기반 요청 제한이 이 동작에 의존한다.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.forward-headers-strategy=framework"
)
class ForwardedHeaderTest {

    private static final String FORWARDED_CLIENT_IP = "203.0.113.7";

    @LocalServerPort
    private int port;

    @Test
    void XForwardedFor헤더가있으면_실제클라이언트IP를반환한다() throws Exception {
        // when
        String remoteAddr = callRemoteAddr(FORWARDED_CLIENT_IP);

        // then
        assertThat(remoteAddr).isEqualTo(FORWARDED_CLIENT_IP);
    }

    @Test
    void XForwardedFor헤더가없으면_직접연결IP를반환한다() throws Exception {
        // when
        String remoteAddr = callRemoteAddr(null);

        // then
        assertThat(remoteAddr).isNotBlank();
        assertThat(remoteAddr).isNotEqualTo(FORWARDED_CLIENT_IP);
    }

    private String callRemoteAddr(String forwardedFor) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/test/remote-addr"))
                .GET();

        if (forwardedFor != null) {
            builder.header("X-Forwarded-For", forwardedFor);
        }

        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
        }
    }

    @TestConfiguration
    static class RemoteAddrControllerConfig {

        @Bean
        RemoteAddrController remoteAddrController() {
            return new RemoteAddrController();
        }
    }

    @RestController
    static class RemoteAddrController {

        @GetMapping("/test/remote-addr")
        String remoteAddr(HttpServletRequest request) {
            return request.getRemoteAddr();
        }
    }
}
