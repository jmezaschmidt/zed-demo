package com.example.shortener.core;

import com.example.shortener.errors.CapacityExceededException;

/**
 * Abstraction over ID generation. Implementations allocate 48-bit IDs in a
 * thread-safe manner and may enforce upper bounds on the ID space.
 */
public interface IdSpace {
    /**
     * Allocates and returns the next 48-bit ID. If the ID space is exhausted,
     * a {@link CapacityExceededException} must be thrown.
     *
     * @return a monotonic ID in the range [0, 2^48 - 1]
     * @throws CapacityExceededException if no IDs remain
     */
    long allocate() throws CapacityExceededException;

    /**
     * Returns the maximum ID that can be allocated from this space.
     */
    long maxId();

    /**
     * Returns a best-effort estimate of remaining IDs. Implementations
     * may return approximate values.
     */
    long remaining();
}