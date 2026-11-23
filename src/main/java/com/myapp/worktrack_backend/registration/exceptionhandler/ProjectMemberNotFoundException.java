package com.myapp.worktrack_backend.registration.exceptionhandler;

public class ProjectMemberNotFoundException extends RuntimeException {
    public ProjectMemberNotFoundException(String message) {
        super(message);
    }
}
