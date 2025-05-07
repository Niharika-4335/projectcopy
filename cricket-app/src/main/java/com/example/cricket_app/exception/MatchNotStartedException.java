package com.example.cricket_app.exception;

public class MatchNotStartedException extends RuntimeException{
    public MatchNotStartedException(String message) {
        super(message);
    }
}
