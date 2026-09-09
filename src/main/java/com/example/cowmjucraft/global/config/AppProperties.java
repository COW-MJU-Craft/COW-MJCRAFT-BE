package com.example.cowmjucraft.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicBaseUrl;
    private String orderViewPath;
    private long orderViewTokenTtlMinutes;

    /**
     * 이메일 인증 코드 해싱에 쓰는 서버 비밀값(pepper).
     * DB가 통째로 유출돼도 코드 해시만으로는 원본 6자리를 되돌리지 못하게 한다.
     * 절대 커밋하지 않는다 — 환경변수로만 주입한다.
     */
    private String customerCodePepper;

    /** 인증 코드 유효 시간(분). */
    private long customerCodeTtlMinutes = 10;

    /** 같은 이메일로 코드를 다시 받기까지의 최소 간격(초). */
    private long customerCodeResendIntervalSeconds = 60;

    /** 같은 이메일의 24시간 발급 상한. */
    private int customerCodeDailyLimit = 10;
}
