package com.example.shortener.core;

import com.example.shortener.errors.InvalidUrlException;

/**
 * Interface for URL validation. Implementations should verify that a URL
 * conforms to supported schemes, length constraints and general URI syntax.
 */
public interface UrlValidator {
    /**
     * Validates the given URL string. If the URL is invalid, an
     * {@link InvalidUrlException} is thrown.
     *
     * @param url the URL string to validate
     * @throws InvalidUrlException if the URL is null, empty, too long or malformed
     */
    void validate(String url) throws InvalidUrlException;
}