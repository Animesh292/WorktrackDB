package com.myapp.worktrack_backend.registration.exceptionhandler;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
