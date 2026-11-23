package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DepartmentHeadInEligible extends RuntimeException {
    public DepartmentHeadInEligible(String message) {
        super(message);
    }
}
