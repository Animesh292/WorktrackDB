package com.myapp.worktrack_backend.registration.exceptionhandler;

public class SprintNotFoundException extends RuntimeException {
    public SprintNotFoundException(String message) {
        super(message);
    }
}
