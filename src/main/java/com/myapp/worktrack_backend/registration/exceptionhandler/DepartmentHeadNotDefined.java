package com.myapp.worktrack_backend.registration.exceptionhandler;

public class DepartmentHeadNotDefined extends RuntimeException {
    public DepartmentHeadNotDefined(String message) {
        super(message);
    }
}
