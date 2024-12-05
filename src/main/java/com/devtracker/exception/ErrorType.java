package com.devtracker.exception;

// features/common/exception/ErrorType.java
public enum ErrorType {
    NOT_FOUND("NOT_FOUND", "The requested resource was not found"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Input validation failed"),
    INTERNAL_ERROR("INTERNAL_ERROR", "An internal error occurred"),
    UNAUTHORIZED("UNAUTHORIZED", "Not authorized to perform this action");

    private final String code;
    private final String defaultMessage;

    ErrorType(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}