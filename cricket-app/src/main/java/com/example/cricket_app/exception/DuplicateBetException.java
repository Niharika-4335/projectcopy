package com.example.cricket_app.exception;

public class DuplicateBetException extends RuntimeException {
    public DuplicateBetException(String message) {
        super(message);
    }
}
