package com.dlsexam.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationSeconds;

    public JwtService(
        @Value("${app.jwt.secret}") String base64Secret,
        @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UUID userId, String email, Set<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .claim("uid", userId.toString())
            .claim("roles", roles)
            .signWith(signingKey)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey).build()
            .parseSignedClaims(token).getPayload();
    }

    public Map<String, Object> readClaims(String token) {
        Claims claims = parseClaims(token);
        return Map.copyOf(claims);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
