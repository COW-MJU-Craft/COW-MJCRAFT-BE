package com.example.cowmjucraft.global.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.accounts.Role;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    // 256비트 키를 Base64URL로 인코딩한 값 (JwtProperties의 최소 길이 43자 충족)
    private static final String SECRET = "dGVzdC1zZWNyZXQtZm9yLWp3dC10b2tlbi1wcm92aWRlci0x";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(properties(3600, 7200));
    }

    private JwtProperties properties(long accessSeconds, long refreshSeconds) {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setAccessExpirationSeconds(accessSeconds);
        props.setRefreshExpirationSeconds(refreshSeconds);
        return props;
    }

    @Test
    void generateAdminAccessToken_정상발급_subject와role과타입을담는다() {
        // given & when
        String token = jwtTokenProvider.generateAdminAccessToken("admin");

        // then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getSubject(token)).isEqualTo("admin");
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(Role.ROLE_ADMIN.name());
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("access");
    }

    @Test
    void generateAdminRefreshToken_정상발급_refresh타입으로발급된다() {
        // given & when
        String token = jwtTokenProvider.generateAdminRefreshToken("admin");

        // then
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("refresh");
        assertThat(jwtTokenProvider.validateRefreshToken(token)).isTrue();
        assertThat(jwtTokenProvider.validateAccessToken(token)).isFalse();
    }

    @Test
    void validateAccessToken_access토큰_true를반환한다() {
        // given
        String token = jwtTokenProvider.generateAccessToken("subject", Role.ROLE_ADMIN);

        // when & then
        assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.validateRefreshToken(token)).isFalse();
    }

    /**
     * jjwt 0.12.x의 signWith(key)는 키 길이로 알고리즘을 추론하므로, HS256을 명시하지 않으면
     * 기존에 발급된 토큰과 헤더가 달라진다. 업그레이드 후에도 alg가 HS256으로 유지되는지 고정한다.
     */
    @Test
    void generateAccessToken_서명알고리즘_HS256을유지한다() {
        // given
        String token = jwtTokenProvider.generateAccessToken("subject", Role.ROLE_ADMIN);

        // when
        String header = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                StandardCharsets.UTF_8
        );

        // then
        assertThat(header).contains("\"alg\":\"HS256\"");
    }

    @Test
    void validateToken_서명변조_false를반환한다() {
        // given
        String token = jwtTokenProvider.generateAdminAccessToken("admin");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "ZmFrZS1zaWduYXR1cmU";

        // when & then
        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_다른키로서명된토큰_false를반환한다() {
        // given
        JwtTokenProvider other = new JwtTokenProvider(otherKeyProperties());
        String foreignToken = other.generateAdminAccessToken("admin");

        // when & then
        assertThat(jwtTokenProvider.validateToken(foreignToken)).isFalse();
    }

    private JwtProperties otherKeyProperties() {
        JwtProperties props = new JwtProperties();
        props.setSecret("YW5vdGhlci1zZWNyZXQtZm9yLWp3dC10b2tlbi1wcm92aWRlci0y");
        props.setAccessExpirationSeconds(3600);
        props.setRefreshExpirationSeconds(7200);
        return props;
    }

    @Test
    void validateToken_만료된토큰_false를반환한다() {
        // given
        JwtTokenProvider expiring = new JwtTokenProvider(properties(-1, -1));
        String expired = expiring.generateAdminAccessToken("admin");

        // when & then
        assertThat(expiring.validateToken(expired)).isFalse();
        assertThat(expiring.validateAccessToken(expired)).isFalse();
    }

    @Test
    void validateToken_형식이아닌문자열_false를반환한다() {
        // given & when & then
        assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }
}
