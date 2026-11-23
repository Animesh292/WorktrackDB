package com.myapp.worktrack_backend.registration.exceptionhandler;

public class AccountDeactivated extends RuntimeException {
    public AccountDeactivated(String message) {
        super(message);
    }
}
