package com.example.shortener.core;

/**
 * A no-op normalizer that returns the input URL unchanged. Useful when
 * normalization is not desired or is deferred to a later phase.
 */
public class NoOpUrlNormalizer implements UrlNormalizer {
    @Override
    public String normalize(String url) {
        return url;
    }
}