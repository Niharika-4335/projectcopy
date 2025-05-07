package com.example.cricket_app.exception;

public class MatchWinnerNotDeclaredException extends RuntimeException {
    public MatchWinnerNotDeclaredException(String message) {
        super(message);
    }
}
