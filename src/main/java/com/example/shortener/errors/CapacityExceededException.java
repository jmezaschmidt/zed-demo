package com.example.shortener.errors;

/**
 * Thrown when the ID space has been exhausted and no further IDs can be
 * allocated.
 */
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException() {
        super();
    }

    public CapacityExceededException(String message) {
        super(message);
    }

    public CapacityExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}