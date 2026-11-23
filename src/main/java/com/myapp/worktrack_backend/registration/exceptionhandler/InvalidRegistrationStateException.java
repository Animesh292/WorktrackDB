package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidRegistrationStateException extends RuntimeException{
    public InvalidRegistrationStateException(String message) {
        super(message);
    }
}
