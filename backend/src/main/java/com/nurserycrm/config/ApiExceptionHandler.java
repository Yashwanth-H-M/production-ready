package com.nurserycrm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException exception) {
        log.warn("Invalid request: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", exception.getMessage() != null ? exception.getMessage() : "Invalid request"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidState(IllegalStateException exception) {
        log.warn("Invalid state: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", exception.getMessage() != null ? exception.getMessage() : "Invalid state"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnhandled(Exception exception) {
        log.error("Unhandled API exception: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", exception.getMessage() != null ? exception.getMessage() : "Internal server error"));
    }
}
