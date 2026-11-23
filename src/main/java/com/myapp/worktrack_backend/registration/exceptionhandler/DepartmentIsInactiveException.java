package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DepartmentIsInactiveException extends RuntimeException {
    public DepartmentIsInactiveException(String message) {
        super(message);
    }
}
