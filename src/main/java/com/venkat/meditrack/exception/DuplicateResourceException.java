package com.venkat.meditrack.exception;

// Mapped to HTTP 409 by GlobalExceptionHandler (e.g. duplicate email/licence).
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
