package com.dlsexam.userservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String TEST_SECRET_BASE64 = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void generateAndParseClaimsContainExpectedIdentityData() {
        JwtService jwtService = new JwtService(TEST_SECRET_BASE64, 1800L);
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "alice@example.com", Set.of("USER", "ADMIN"));
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("alice@example.com");
        assertThat(claims.get("uid", String.class)).isEqualTo(userId.toString());
        assertThat(claims.get("roles", List.class)).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void parseClaimsRejectsTamperedToken() {
        JwtService jwtService = new JwtService(TEST_SECRET_BASE64, 1800L);
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "alice@example.com", Set.of("USER"));

        assertThrows(Exception.class, () -> jwtService.parseClaims(token + "tamper"));
    }
}

