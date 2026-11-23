package com.myapp.worktrack_backend.registration.exceptionhandler;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
