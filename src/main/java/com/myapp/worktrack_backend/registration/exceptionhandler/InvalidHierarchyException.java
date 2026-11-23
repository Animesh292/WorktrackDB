package com.myapp.worktrack_backend.registration.exceptionhandler;

public class InvalidHierarchyException extends RuntimeException {
    public InvalidHierarchyException(String message) {
        super(message);
    }
}
