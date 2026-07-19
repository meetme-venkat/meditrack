package com.venkat.meditrack.exception;

// Mapped to HTTP 422 by GlobalExceptionHandler (e.g. no slots available).
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
