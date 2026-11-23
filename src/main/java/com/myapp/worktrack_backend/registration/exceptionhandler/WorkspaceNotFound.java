package com.myapp.worktrack_backend.registration.exceptionhandler;

public class WorkspaceNotFound extends RuntimeException {
    public WorkspaceNotFound(String message) {
        super(message);
    }
}
