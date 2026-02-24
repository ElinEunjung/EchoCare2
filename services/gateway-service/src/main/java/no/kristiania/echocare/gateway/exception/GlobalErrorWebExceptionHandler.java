package no.kristiania.echocare.gateway.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * Global error handler for the API Gateway.
 * Catches exceptions that occur during request processing and returns
 * appropriate HTTP status codes with JSON error responses.
 */
@Slf4j
@Component
@Order(-2) // High priority to handle errors before default handlers
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error occurred: {}", ex.getMessage(), ex);

        HttpStatus status;
        String message;

        // Determine status code based on exception type
        if (ex instanceof JwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid or expired JWT token";
        } else if (ex instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An internal error occurred";
        }

        // Build JSON error response
        String jsonError = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
                status.getReasonPhrase(),
                message,
                status.value()
        );

        // Set response headers
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Write response body
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(jsonError.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

