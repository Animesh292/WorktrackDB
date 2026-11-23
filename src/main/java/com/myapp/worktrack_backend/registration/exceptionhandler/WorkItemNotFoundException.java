package com.myapp.worktrack_backend.registration.exceptionhandler;

public class WorkItemNotFoundException extends RuntimeException {
    public WorkItemNotFoundException(String message) {
        super(message);
    }
}
