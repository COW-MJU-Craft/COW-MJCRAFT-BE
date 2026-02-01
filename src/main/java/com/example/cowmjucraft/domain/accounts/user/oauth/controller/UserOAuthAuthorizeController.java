package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import com.example.cowmjucraft.domain.accounts.user.oauth.config.KakaoOAuthProperties;
import com.example.cowmjucraft.domain.accounts.user.oauth.config.NaverOAuthProperties;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/oauth")
public class UserOAuthAuthorizeController implements UserOAuthAuthorizeControllerDocs {

    private static final String KAKAO_AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String NAVER_AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    private static final String RESPONSE_TYPE_CODE = "code";

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final NaverOAuthProperties naverOAuthProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/kakao/authorize")
    @Override
    public ResponseEntity<Void> authorizeKakao() {
        assertConfigured(
                kakaoOAuthProperties.getClientId(),
                kakaoOAuthProperties.getRedirectUri(),
                "kakao"
        );

        String state = generateState();

        String redirectUrl = UriComponentsBuilder
                .fromUriString(KAKAO_AUTHORIZE_URL)
                .queryParam("response_type", RESPONSE_TYPE_CODE)
                .queryParam("client_id", kakaoOAuthProperties.getClientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                .queryParam("state", state)
                .build(true)
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/naver/authorize")
    @Override
    public ResponseEntity<Void> authorizeNaver() {
        assertConfigured(
                naverOAuthProperties.getClientId(),
                naverOAuthProperties.getRedirectUri(),
                "naver"
        );

        String state = generateState();

        String redirectUrl = UriComponentsBuilder
                .fromUriString(NAVER_AUTHORIZE_URL)
                .queryParam("response_type", RESPONSE_TYPE_CODE)
                .queryParam("client_id", naverOAuthProperties.getClientId())
                .queryParam("redirect_uri", naverOAuthProperties.getRedirectUri())
                .queryParam("state", state)
                .build(true)
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    private void assertConfigured(String clientId, String redirectUri, String provider) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    provider + " clientId is not configured"
            );
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    provider + " redirectUri is not configured"
            );
        }
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
