package com.example.cowmjucraft.domain.accounts.user.oauth.client;

import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import com.example.cowmjucraft.domain.accounts.user.oauth.config.NaverOAuthProperties;
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
public class NaverOAuthClient {

    private final NaverOAuthProperties properties;
    private final RestClient restClient = RestClient.builder().build();

    public NaverUserInfo fetchUserInfo(String code, String state) {
        String accessToken = requestAccessToken(code, state);
        NaverUserResponse response = restClient.get()
                .uri(properties.getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(NaverUserResponse.class);

        if (response == null || response.response() == null || response.response().id() == null) {
            throw new AccountException(AccountErrorType.OAUTH_PROFILE_FETCH_FAILED);
        }

        NaverUserProfile profile = response.response();
        return new NaverUserInfo(profile.id(), profile.email(), profile.nickname());
    }

    private String requestAccessToken(String code, String state) {
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
        if (state != null && !state.isBlank()) {
            form.add("state", state);
        }

        NaverTokenResponse response = restClient.post()
                .uri(properties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(NaverTokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new AccountException(AccountErrorType.OAUTH_TOKEN_FETCH_FAILED);
        }

        return response.accessToken();
    }

    public record NaverUserInfo(String providerId, String email, String nickname) {
    }

    private record NaverTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record NaverUserResponse(String resultcode, String message, NaverUserProfile response) {
    }

    private record NaverUserProfile(
            String id,
            String email,
            String nickname,
            @JsonProperty("profile_image") String profileImage
    ) {
    }
}
