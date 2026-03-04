package com.example.cowmjucraft.global.config.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank(message = "jwt.secret must not be blank (env: JWT_SECRET)")
    @Size(min = 43, message = "jwt.secret must be at least 43 Base64URL chars (= 256-bit key) (env: JWT_SECRET)")
    private String secret;

    @Min(value = 1, message = "jwt.access-expiration-seconds must be >= 1 (env: JWT_ACCESS_EXPIRATION_SECONDS)")
    private long accessExpirationSeconds;

    @Min(value = 1, message = "jwt.refresh-expiration-seconds must be >= 1 (env: JWT_REFRESH_EXPIRATION_SECONDS)")
    private long refreshExpirationSeconds;
}
