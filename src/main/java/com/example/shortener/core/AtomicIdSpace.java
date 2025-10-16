package com.example.shortener.core;

import com.example.shortener.errors.CapacityExceededException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple ID space that uses an {@link AtomicLong} counter. IDs are allocated
 * starting from 0 up to the maximum inclusive value of 2^48 - 1. If the
 * counter exceeds this bound, a {@link CapacityExceededException} is thrown.
 */
public class AtomicIdSpace implements IdSpace {
    private static final long MAX_ID = (1L << 48) - 1;
    private final AtomicLong counter = new AtomicLong(0L);

    @Override
    public long allocate() throws CapacityExceededException {
        long next = counter.getAndIncrement();
        if (next > MAX_ID) {
            throw new CapacityExceededException("ID space exhausted");
        }
        return next;
    }

    @Override
    public long maxId() {
        return MAX_ID;
    }

    @Override
    public long remaining() {
        long current = counter.get();
        long remaining = MAX_ID - current;
        return remaining < 0 ? 0 : remaining;
    }
}