package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DuplicateMembershipException extends RuntimeException {
    public DuplicateMembershipException(String message) {
        super(message);
    }
}
