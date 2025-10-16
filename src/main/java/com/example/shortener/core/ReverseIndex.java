package com.example.shortener.core;

import java.util.Optional;

/**
 * Reverse mapping from a URL to an ID. Used to ensure idempotency within a
 * single runtime. Does not need to support removal.
 */
public interface ReverseIndex {
    /**
     * Retrieves the ID previously associated with the given normalized URL, if
     * present.
     *
     * @param normalizedUrl a validated and normalized URL
     * @return an Optional containing the ID if present, otherwise empty
     */
    Optional<Long> getId(String normalizedUrl);

    /**
     * Associates the given URL with the specified ID if it is not already
     * present. Returns the ID that is ultimately stored.
     *
     * @param normalizedUrl a validated and normalized URL
     * @param id the ID to associate
     * @return the ID actually stored (may be the input or an existing one)
     */
    long putIfAbsent(String normalizedUrl, long id);
}