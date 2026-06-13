package com.dlsexam.catalogservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey).build()
            .parseSignedClaims(token).getPayload();
    }

    public Set<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of("USER");
    }
}
