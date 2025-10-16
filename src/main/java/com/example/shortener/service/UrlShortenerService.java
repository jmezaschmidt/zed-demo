package com.example.shortener.service;

import com.example.shortener.errors.CapacityExceededException;
import com.example.shortener.errors.InvalidUrlException;

import java.util.Optional;

/**
 * Service API for shortening URLs and resolving short codes.
 */
public interface UrlShortenerService {
    /**
     * Shortens the provided URL and returns a short code. The same URL returns the
     * same code within the lifetime of the service.
     *
     * @param longUrl the URL to shorten
     * @return the generated short code
     * @throws InvalidUrlException if the input is null, empty or invalid
     * @throws CapacityExceededException if the internal ID space is exhausted
     */
    String shorten(String longUrl) throws InvalidUrlException, CapacityExceededException;

    /**
     * Resolves the given short code back to the original URL.
     *
     * @param code the 8-character short code
     * @return an Optional containing the URL if known, or empty if not found
     */
    Optional<String> resolve(String code);
}