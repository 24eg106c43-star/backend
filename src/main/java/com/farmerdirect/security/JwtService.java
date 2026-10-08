package com.farmerdirect.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }
    public String generate(String email, String role, long profileId) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(email).claim("role", role).claim("pid", profileId)
                .issuedAt(new Date(now)).expiration(new Date(now + expirationMs)).signWith(key).compact();
    }
    public Claims parse(String token) {   // throws if invalid or expired
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
