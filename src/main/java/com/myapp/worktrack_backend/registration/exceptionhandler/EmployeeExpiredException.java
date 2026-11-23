package com.myapp.worktrack_backend.registration.exceptionhandler;

public class EmployeeExpiredException extends RuntimeException {
    public EmployeeExpiredException(String message) {
        super(message);
    }
}
