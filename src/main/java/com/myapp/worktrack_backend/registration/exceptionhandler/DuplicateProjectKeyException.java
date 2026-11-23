package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DuplicateProjectKeyException extends RuntimeException {
    public DuplicateProjectKeyException(String message) {
        super(message);
    }
}
