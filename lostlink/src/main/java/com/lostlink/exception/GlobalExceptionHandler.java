
package com.lostlink.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackages = "com.lostlink.controller")
public class GlobalExceptionHandler {

    // 404 - Item not found
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<?> handleItemNotFound(
            ItemNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not Found",
                        "message", exception.getMessage()));
    }

    // 400 - Invalid item details
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Bad Request",
                        "message", exception.getMessage()));
    }

    // 400 - Bean Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message", "Validation failed",
                        "errors", errors));
    }

    // 403 - User does not own the item
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(
            AccessDeniedException exception) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "Forbidden",
                        "message", exception.getMessage()));
    }

    // Other HTTP errors, including 409 Conflict
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(
            ResponseStatusException exception) {

        String message = exception.getReason() == null
                ? "Request could not be completed."
                : exception.getReason();

        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of(
                        "error", "Request Failed",
                        "message", message));
    }
}
