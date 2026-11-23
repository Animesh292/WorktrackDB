package com.myapp.worktrack_backend.registration.exceptionhandler;

public class WorkspaceIsInactiveException extends RuntimeException {
    public WorkspaceIsInactiveException(String message) {
        super(message);
    }
}
