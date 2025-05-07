package com.example.cricket_app.exception;

public class InvalidTeamChosenException extends RuntimeException {
    public InvalidTeamChosenException(String message) {
        super(message);
    }
}
