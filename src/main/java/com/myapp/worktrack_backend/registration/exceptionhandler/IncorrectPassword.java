package com.myapp.worktrack_backend.registration.exceptionhandler;

public class IncorrectPassword extends RuntimeException {
    public IncorrectPassword(String message) {
        super(message);
    }
}
