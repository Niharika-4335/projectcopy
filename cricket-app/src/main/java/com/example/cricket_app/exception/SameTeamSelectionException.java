package com.example.cricket_app.exception;

public class SameTeamSelectionException extends RuntimeException{
    public SameTeamSelectionException(String message) {
        super(message);
    }
}
