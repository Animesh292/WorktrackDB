package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DepartmentNotFoundException extends RuntimeException {
    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
