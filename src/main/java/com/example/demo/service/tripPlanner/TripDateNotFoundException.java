package com.example.demo.service.tripPlanner;

public class TripDateNotFoundException extends RuntimeException {
    public TripDateNotFoundException(String message) {
        super(message);
    }
}
