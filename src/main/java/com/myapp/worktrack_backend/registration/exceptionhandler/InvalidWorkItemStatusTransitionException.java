package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidWorkItemStatusTransitionException extends RuntimeException {
    public InvalidWorkItemStatusTransitionException(String message) {
        super(message);
    }
}
