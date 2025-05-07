package com.example.cricket_app.exception;

public class AdminNotFoundException extends RuntimeException  {
    public AdminNotFoundException(String message) {
        super(message);
    }
}
