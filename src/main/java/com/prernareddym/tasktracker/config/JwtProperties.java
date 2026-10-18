package com.prernareddym.tasktracker.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT settings. The secret must be supplied through the JWT_SECRET environment variable;
 * the application refuses to start without a sufficiently long one.
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "app.jwt.secret must be at least 32 characters") String secret,
        @Min(1) long expirationMinutes) {
}
