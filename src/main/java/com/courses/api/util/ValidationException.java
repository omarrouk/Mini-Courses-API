package com.courses.api.util;

public class ValidationException extends RuntimeException {
    private final String errorCode;

    public ValidationException(String message) {
        this("VALIDATION_ERROR", message);
    }

    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
