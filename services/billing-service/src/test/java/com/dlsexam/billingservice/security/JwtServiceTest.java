package com.dlsexam.billingservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "VGVzdGluZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw==";

    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void parseClaims_withValidToken_returnsExpectedClaims() {
        String token = Jwts.builder()
            .subject("alice@example.com")
            .claim("uid", "f0d3ab3e-9ccf-4902-8c18-633ab0f9d147")
            .claim("roles", List.of("ADMIN", "USER"))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .compact();

        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("alice@example.com");
        assertThat(claims.get("uid", String.class)).isEqualTo("f0d3ab3e-9ccf-4902-8c18-633ab0f9d147");
        assertThat(jwtService.extractRoles(claims)).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void parseClaims_withTamperedToken_throwsException() {
        String otherSecret = "QW5vdGhlclRlc3RTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw==";
        String token = Jwts.builder()
            .subject("alice@example.com")
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(otherSecret)))
            .compact();

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void extractRoles_withoutRolesClaim_fallsBackToUserRole() {
        String token = Jwts.builder()
            .subject("bob@example.com")
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .compact();

        Claims claims = jwtService.parseClaims(token);

        assertThat(jwtService.extractRoles(claims)).containsExactly("USER");
    }
}

