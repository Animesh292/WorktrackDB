package com.myapp.worktrack_backend.registration.exceptionhandler;

public class ActiveSprintExistsException extends RuntimeException {
    public ActiveSprintExistsException(String message) {
        super(message);
    }
}
