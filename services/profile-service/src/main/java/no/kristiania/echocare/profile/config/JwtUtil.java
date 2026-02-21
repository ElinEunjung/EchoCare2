package no.kristiania.echocare.profile.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Decode Base64 secret from application.yml
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    // Token valid for 24 hours
    private static final long EXPIRATION_TIME_MS = 86400000;

    public String generateToken(UUID caregiverId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME_MS);

        return Jwts.builder()
                .subject(caregiverId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public UUID extractCaregiverId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
