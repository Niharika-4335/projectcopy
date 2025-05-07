package com.example.cricket_app.exception;

public class PayoutNotFoundException extends RuntimeException {
    public PayoutNotFoundException(String message) {
        super(message);
    }
}
