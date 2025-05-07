package com.example.cricket_app.exception;

public class NonPositiveAmountException extends RuntimeException{
    public NonPositiveAmountException(String message) {
        super(message);
    }
}
