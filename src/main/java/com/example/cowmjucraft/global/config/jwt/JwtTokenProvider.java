package com.example.cowmjucraft.global.config.jwt;

import com.example.cowmjucraft.domain.accounts.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtTokenProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(props.getSecret()));
        this.accessExpirationSeconds = props.getAccessExpirationSeconds();
        this.refreshExpirationSeconds = props.getRefreshExpirationSeconds();
    }

    public String generateAdminAccessToken(String loginId) {
        return generateToken(loginId, Role.ROLE_ADMIN, TOKEN_TYPE_ACCESS, accessExpirationSeconds);
    }

    public String generateAdminRefreshToken(String loginId) {
        return generateToken(loginId, Role.ROLE_ADMIN, TOKEN_TYPE_REFRESH, refreshExpirationSeconds);
    }

    public String generateAccessToken(String subject, Role role) {
        return generateToken(subject, role, TOKEN_TYPE_ACCESS, accessExpirationSeconds);
    }

    public String generateRefreshToken(String subject, Role role) {
        return generateToken(subject, role, TOKEN_TYPE_REFRESH, refreshExpirationSeconds);
    }

    private String generateToken(String subject, Role role, String tokenType, long expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .subject(subject)
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                // 0.12.x의 signWith(key)는 키 길이로 알고리즘을 추론한다. 기존 토큰과 헤더를
                // 동일하게 유지하려면 HS256을 명시해야 한다.
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).getPayload().get(CLAIM_ROLE);
        return role == null ? null : role.toString();
    }

    public String getTokenType(String token) {
        Object tokenType = parseClaims(token).getPayload().get(CLAIM_TOKEN_TYPE);
        return tokenType == null ? null : tokenType.toString();
    }

    public boolean validateAccessToken(String token) {
        return validateTokenByType(token, TOKEN_TYPE_ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenByType(token, TOKEN_TYPE_REFRESH);
    }

    private boolean validateTokenByType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token).getPayload();
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return expectedType.equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
