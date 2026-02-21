package no.kristiania.echocare.playlist.api.controller;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.echocare.playlist.exception.NoSongsAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSongsAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoSongsAvailable(NoSongsAvailableException ex) {
        log.error("No songs available: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("error", ex.getMessage());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("message", "No songs match the criteria. Try different parameters.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("error", ex.getMessage());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred", ex);
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("error", ex.getMessage());
        error.put("type", ex.getClass().getSimpleName());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // Include stack trace for debugging (remove in production)
        if (log.isDebugEnabled()) {
            error.put("stackTrace", getStackTraceAsString(ex));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("error", "An unexpected error occurred");
        error.put("message", ex.getMessage());
        error.put("type", ex.getClass().getSimpleName());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // Include stack trace for debugging
        if (log.isDebugEnabled()) {
            error.put("stackTrace", getStackTraceAsString(ex));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        if (ex.getCause() != null) {
            sb.append("Caused by: ").append(getCauseStackTrace(ex.getCause()));
        }
        return sb.toString();
    }

    private String getCauseStackTrace(Throwable cause) {
        StringBuilder sb = new StringBuilder();
        sb.append(cause.toString()).append("\n");
        for (StackTraceElement element : cause.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}

