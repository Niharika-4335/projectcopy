package com.example.cricket_app.exception;

public class WinnerAlreadyDeclaredException extends RuntimeException{
    public WinnerAlreadyDeclaredException(String message) {
        super(message);
    }
}
