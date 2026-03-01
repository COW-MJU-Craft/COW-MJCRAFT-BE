package com.example.cowmjucraft.global.config.jwt;

import com.example.cowmjucraft.domain.accounts.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final Key key;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-seconds}") long accessExpirationSeconds,
            @Value("${jwt.refresh-expiration-seconds}") long refreshExpirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String generateAdminAccessToken(String loginId) {
        return generateToken(loginId, Role.ROLE_ADMIN, TOKEN_TYPE_ACCESS, accessExpirationSeconds);
    }

    public String generateAdminRefreshToken(String loginId) {
        return generateToken(loginId, Role.ROLE_ADMIN, TOKEN_TYPE_REFRESH, refreshExpirationSeconds);
    }

    public String generateMemberAccessToken(Long memberId) {
        return generateToken(String.valueOf(memberId), Role.ROLE_USER, TOKEN_TYPE_ACCESS, accessExpirationSeconds);
    }

    public String generateMemberRefreshToken(Long memberId) {
        return generateToken(String.valueOf(memberId), Role.ROLE_USER, TOKEN_TYPE_REFRESH, refreshExpirationSeconds);
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
                .setSubject(subject)
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
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
        return parseClaims(token).getBody().getSubject();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).getBody().get(CLAIM_ROLE);
        return role == null ? null : role.toString();
    }

    public String getTokenType(String token) {
        Object tokenType = parseClaims(token).getBody().get(CLAIM_TOKEN_TYPE);
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
            Claims claims = parseClaims(token).getBody();
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
