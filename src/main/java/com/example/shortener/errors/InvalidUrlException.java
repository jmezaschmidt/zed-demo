package com.example.shortener.errors;

/**
 * Thrown when a URL is null, empty, exceeds the maximum length, has an
 * unsupported scheme, or is otherwise malformed.
 */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException() {
        super();
    }

    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}