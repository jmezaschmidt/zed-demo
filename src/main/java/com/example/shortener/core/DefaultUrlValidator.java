package com.example.shortener.core;

import com.example.shortener.errors.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Default validator for URLs. Ensures that the URL is non-null, non-empty,
 * under a configurable maximum length and uses an allowed scheme.
 */
public class DefaultUrlValidator implements UrlValidator {
    private final int maxLength;
    private final Set<String> allowedSchemes;

    /**
     * Creates a new validator.
     *
     * @param maxLength maximum allowed URL length
     * @param allowedSchemes set of lowercase schemes (e.g. http, https)
     */
    public DefaultUrlValidator(int maxLength, Set<String> allowedSchemes) {
        this.maxLength = maxLength;
        this.allowedSchemes = allowedSchemes;
    }

    @Override
    public void validate(String url) throws InvalidUrlException {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL must be non-empty");
        }
        if (url.length() > maxLength) {
            throw new InvalidUrlException("URL exceeds maximum length of " + maxLength);
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Malformed URL", e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !allowedSchemes.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException("Unsupported URL scheme: " + scheme);
        }
    }
}