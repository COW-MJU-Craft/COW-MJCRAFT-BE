package com.example.cowmjucraft.domain.analytics.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google Analytics 4 Data API 연동 설정.
 *
 * <p>값이 비어 있으면 분석 API만 503으로 응답하고 서버는 정상 기동한다.
 * 서비스 계정 키는 절대 커밋하지 않고 환경변수로만 주입한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "analytics.ga")
public class GaProperties {

    /** GA4 속성 ID (숫자). */
    private String propertyId = "";

    /** 서비스 계정 키 JSON을 Base64로 인코딩한 값. 읽기 전용(Viewer) 계정을 쓴다. */
    private String credentialsBase64 = "";

    /** 같은 조회 기간의 결과를 재사용하는 시간. */
    private Duration cacheTtl = Duration.ofMinutes(15);

    public boolean isConfigured() {
        return propertyId != null && !propertyId.isBlank()
                && credentialsBase64 != null && !credentialsBase64.isBlank();
    }
}
