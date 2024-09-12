package com.example.demo.service.planner;

public class TripDateNotFoundException extends RuntimeException {
    public TripDateNotFoundException(String message) {
        super(message);
    }
}
