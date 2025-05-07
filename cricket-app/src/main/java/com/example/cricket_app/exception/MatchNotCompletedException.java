package com.example.cricket_app.exception;

public class MatchNotCompletedException extends RuntimeException{
    public MatchNotCompletedException(String message) {
        super(message);
    }
}
