package com.example.shortener.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of {@link ReverseIndex} using a
 * {@link ConcurrentHashMap}. Expected average lookup time is O(1), though
 * worst-case performance may degrade with malicious inputs; however, this map
 * is only used on the shorten path, so it does not affect resolve-time
 * guarantees.
 */
public class ConcurrentReverseIndex implements ReverseIndex {
    private final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    @Override
    public Optional<Long> getId(String normalizedUrl) {
        Long id = map.get(normalizedUrl);
        return Optional.ofNullable(id);
    }

    @Override
    public long putIfAbsent(String normalizedUrl, long id) {
        return map.computeIfAbsent(normalizedUrl, k -> id);
    }
}