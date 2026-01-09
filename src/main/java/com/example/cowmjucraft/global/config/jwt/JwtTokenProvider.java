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

    private final Key key;
    private final long expirationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateAdminToken(String loginId) {
        return generateToken(loginId, Role.ROLE_ADMIN);
    }

    public String generateMemberToken(String userId) {
        return generateToken(userId, Role.ROLE_USER);
    }

    private String generateToken(String subject, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role.name())
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
        Object role = parseClaims(token).getBody().get("role");
        return role == null ? null : role.toString();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
