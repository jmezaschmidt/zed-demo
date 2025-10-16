package com.example.shortener.core;

/**
 * Normalization policy for URLs. Implementations may canonicalize URLs
 * according to specific rules (trimming, lowercasing host, removing default
 * ports, etc.). The default implementation is a no-op.
 */
public interface UrlNormalizer {
    /**
     * Returns a normalized representation of the given URL string. The returned
     * value should be suitable for use as a map key in the reverse index.
     *
     * @param url a validated URL string
     * @return a normalized form of the URL
     */
    String normalize(String url);
}