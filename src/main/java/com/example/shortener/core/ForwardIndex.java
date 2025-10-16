package com.example.shortener.core;

import java.util.Optional;

/**
 * Forward mapping from an ID to a URL. Implementations must ensure that
 * {@link #get(long)} operates in constant time regardless of the number of
 * entries stored. A {@link ForwardIndex} is append-only; entries are never
 * removed.
 */
public interface ForwardIndex {
    /**
     * Stores a URL at the specified ID. The ID must be within the range
     * supported by the underlying implementation.
     *
     * @param id 48-bit non-negative ID
     * @param url normalized URL to store
     */
    void put(long id, String url);

    /**
     * Retrieves the URL for the given ID. If the ID has not been assigned
     * or is beyond the current high watermark, returns {@link Optional#empty()}.
     *
     * @param id the 48-bit ID
     * @return an Optional containing the URL if present, otherwise empty
     */
    Optional<String> get(long id);

    /**
     * Returns the highest ID that has been stored so far. This is used to
     * cheaply reject unknown IDs before attempting to read the backing array.
     *
     * @return the highest allocated ID
     */
    long highWatermark();
}