package com.example.cowmjucraft.domain.accounts.user.oauth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.naver")
public class NaverOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri = "https://nid.naver.com/oauth2.0/token";
    private String userInfoUri = "https://openapi.naver.com/v1/nid/me";
}
