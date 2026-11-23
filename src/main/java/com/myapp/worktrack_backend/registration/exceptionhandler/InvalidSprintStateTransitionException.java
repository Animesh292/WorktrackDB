package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidSprintStateTransitionException extends RuntimeException {
    public InvalidSprintStateTransitionException(String message) {
        super(message);
    }
}
