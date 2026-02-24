package no.kristiania.echocare.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Gateway filter that validates JWT Bearer tokens on every protected route.
 *
 * Registered as a named filter "JwtAuthenticationFilter" in application.yaml.
 * Routes listed under /api/auth/** are skipped (handled by the open auth-route).
 *
 * On success the authenticated caregiver ID is forwarded downstream via
 * the X-Caregiver-Id header.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String base64Secret) {
        super(Config.class);
        // Secret is Base64-encoded — decode it before creating the HMAC key
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();

                // Auth endpoints are public — skip JWT check
                if (path.startsWith("/api/auth")) {
                    log.debug("Skipping JWT validation for auth endpoint: {}", path);
                    return chain.filter(exchange);
                }

                log.debug("Validating JWT for: {}", path);

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                    log.warn("Missing or invalid Authorization header for path: {}", path);
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7); // Remove "Bearer " prefix

                try {
                    // JJWT 0.12.x API
                    Claims claims = Jwts.parser()
                            .verifyWith(signingKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String caregiverId = claims.getSubject();
                    log.debug("JWT validated for caregiver: {}", caregiverId);

                    // Forward the authenticated caregiver ID to downstream services
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-Caregiver-Id", caregiverId)
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());

                } catch (JwtException | IllegalArgumentException e) {
                    log.warn("JWT validation failed for path '{}': {}", path, e.getMessage());
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception e) {
                log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    /** Config class required by AbstractGatewayFilterFactory (no fields needed). */
    public static class Config {}
}

