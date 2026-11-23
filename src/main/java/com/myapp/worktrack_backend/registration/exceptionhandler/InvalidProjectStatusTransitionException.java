package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidProjectStatusTransitionException extends RuntimeException {
    public InvalidProjectStatusTransitionException(String message) {
        super(message);
    }
}
