package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidRoleException extends RuntimeException {

    public InvalidRoleException(String message) {
        super(message);
    }
}
