package com.example.demo.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<String> handlePostNotFoundException(ComponentNotFoundException ex) {
        log.error("ComponentNotFoundException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> handleSignatureException(SignatureException ex) {
        log.error("SignatureException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> handleMalformedJwtException(MalformedJwtException ex) {
        log.error("MalformedJwtException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        log.error("ExpiredJwtException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(RedisConnectionException.class)
    public ResponseEntity<String> handleRedisConnectionException(RedisConnectionException ex) {
        log.error("RedisConnectionException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(UnAuthorizedUserException.class)
    public ResponseEntity<String> handleUnAuthorizedUserException(UnAuthorizedUserException ex) {
        log.error("UnAuthorizedUserException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<String> handleIndexOutOfBoundsException(IndexOutOfBoundsException ex) {
        log.error("IndexOutOfBoundsException Occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException Occurred: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.error("Validation error in field '{}': {}", fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}