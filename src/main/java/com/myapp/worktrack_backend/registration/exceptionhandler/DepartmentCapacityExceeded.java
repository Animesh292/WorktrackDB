package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DepartmentCapacityExceeded extends RuntimeException {
    public DepartmentCapacityExceeded(String message) {
        super(message);
    }
}
