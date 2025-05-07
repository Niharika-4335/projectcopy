package com.example.cricket_app.exception;

public class MatchStartTimeInPastException extends RuntimeException{
    public MatchStartTimeInPastException(String message) {
        super(message);
    }
}
