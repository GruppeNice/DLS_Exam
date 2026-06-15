package com.dlsexam.catalogservice.security;

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
            .subject("catalog@example.com")
            .claim("uid", "8f0b48bd-b5ae-4fc4-a1b8-c9e35c5d6f9a")
            .claim("roles", List.of("ADMIN", "EDITOR"))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .compact();

        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("catalog@example.com");
        assertThat(claims.get("uid", String.class)).isEqualTo("8f0b48bd-b5ae-4fc4-a1b8-c9e35c5d6f9a");
        assertThat(jwtService.extractRoles(claims)).containsExactlyInAnyOrder("ADMIN", "EDITOR");
    }

    @Test
    void parseClaims_withInvalidSignature_throwsException() {
        String otherSecret = "QW5vdGhlclRlc3RTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw==";
        String token = Jwts.builder()
            .subject("catalog@example.com")
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(otherSecret)))
            .compact();

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void extractRoles_withoutRolesClaim_defaultsToUserRole() {
        String token = Jwts.builder()
            .subject("viewer@example.com")
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .compact();

        Claims claims = jwtService.parseClaims(token);

        assertThat(jwtService.extractRoles(claims)).containsExactly("USER");
    }
}

