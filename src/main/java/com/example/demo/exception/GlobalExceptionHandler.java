package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<String> handlePostNotFoundException(ComponentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handleTokenExpiredException(TokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<String> handleTokenInvalidException(TokenInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UnAuthorizedUserException.class)
    public ResponseEntity<String> handleUnAuthorizedUserException(UnAuthorizedUserException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<String> handleIndexOutOfBoundsException(IndexOutOfBoundsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}