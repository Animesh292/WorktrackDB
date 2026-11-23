package com.myapp.worktrack_backend.registration.exceptionhandler;

public class RegistrationNumberNotFoundException extends RuntimeException {

    public RegistrationNumberNotFoundException(String message) {
        super(message);
    }
}
