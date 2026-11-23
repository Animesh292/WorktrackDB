package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DuplicateRegistrationException extends RuntimeException {

    public DuplicateRegistrationException(String message) {
        super(message);
    }
}
