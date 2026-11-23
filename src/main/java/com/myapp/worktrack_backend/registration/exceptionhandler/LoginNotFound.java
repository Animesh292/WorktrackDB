package com.myapp.worktrack_backend.registration.exceptionhandler;

public class LoginNotFound extends RuntimeException {
    public LoginNotFound(String message) {
        super(message);
    }
}
