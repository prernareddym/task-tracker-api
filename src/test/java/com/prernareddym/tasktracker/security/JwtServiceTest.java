package com.prernareddym.tasktracker.security;

import com.prernareddym.tasktracker.config.JwtProperties;
import com.prernareddym.tasktracker.user.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    private final Instant start = Instant.parse("2026-01-01T00:00:00Z");

    private User user() {
        User user = new User("ada@example.com", "hash", "Ada");
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }

    private JwtService service(String secret, Instant now) {
        return new JwtService(new JwtProperties(secret, 10), Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void roundTripsUserIdentity() {
        JwtService service = service(SECRET, start);
        String token = service.generateToken(user());

        assertThat(service.parse(token)).contains(new AuthenticatedUser(42L, "ada@example.com"));
    }

    @Test
    void rejectsExpiredTokens() {
        String token = service(SECRET, start).generateToken(user());

        assertThat(service(SECRET, start.plusSeconds(11 * 60)).parse(token)).isEmpty();
        assertThat(service(SECRET, start.plusSeconds(9 * 60)).parse(token)).isPresent();
    }

    @Test
    void rejectsTokensSignedWithAnotherKey() {
        String token = service("another-secret-another-secret-12345", start).generateToken(user());

        assertThat(service(SECRET, start).parse(token)).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(service(SECRET, start).parse("garbage")).isEmpty();
        assertThat(service(SECRET, start).parse("")).isEmpty();
    }
}
