package com.example.cowmjucraft.domain.accounts.user.oauth.client;

import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import com.example.cowmjucraft.domain.accounts.user.oauth.config.KakaoOAuthProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class KakaoOAuthClient {

    private final KakaoOAuthProperties properties;
    private final RestClient restClient = RestClient.builder().build();

    public KakaoUserInfo fetchUserInfo(String code) {
        String accessToken = requestAccessToken(code);
        KakaoUserResponse response = restClient.get()
                .uri(properties.getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);

        if (response == null || response.id() == null) {
            throw new AccountException(AccountErrorType.OAUTH_PROFILE_FETCH_FAILED);
        }

        String email = null;
        String nickname = null;
        if (response.kakaoAccount() != null) {
            email = response.kakaoAccount().email();
            if (response.kakaoAccount().profile() != null) {
                nickname = response.kakaoAccount().profile().nickname();
            }
        }

        return new KakaoUserInfo(String.valueOf(response.id()), email, nickname);
    }

    private String requestAccessToken(String code) {
        if (properties.getClientId() == null || properties.getClientId().isBlank()) {
            throw new AccountException(AccountErrorType.OAUTH_CLIENT_NOT_CONFIGURED);
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            form.add("client_secret", properties.getClientSecret());
        }
        if (properties.getRedirectUri() != null && !properties.getRedirectUri().isBlank()) {
            form.add("redirect_uri", properties.getRedirectUri());
        }
        form.add("code", code);

        KakaoTokenResponse response = restClient.post()
                .uri(properties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new AccountException(AccountErrorType.OAUTH_TOKEN_FETCH_FAILED);
        }

        return response.accessToken();
    }

    public record KakaoUserInfo(String providerId, String email, String nickname) {
    }

    private record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
    }

    private record KakaoAccount(String email, KakaoProfile profile) {
    }

    private record KakaoProfile(String nickname, @JsonProperty("profile_image_url") String profileImageUrl) {
    }
}
