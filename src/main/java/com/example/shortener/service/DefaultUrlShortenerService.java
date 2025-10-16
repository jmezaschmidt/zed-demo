package com.example.shortener.service;

import com.example.shortener.core.Base64Codec;
import com.example.shortener.core.ForwardIndex;
import com.example.shortener.core.IdSpace;
import com.example.shortener.core.ReverseIndex;
import com.example.shortener.core.UrlNormalizer;
import com.example.shortener.core.UrlValidator;
import com.example.shortener.errors.CapacityExceededException;
import com.example.shortener.errors.InvalidUrlException;

import java.util.Optional;

/**
 * Default implementation of {@link UrlShortenerService} that keeps all state
 * in-memory and guarantees O(1) lookup time for resolves. Codes are generated
 * from a monotonic ID and encoded using a Base64 URL-safe codec without
 * padding.
 */
public class DefaultUrlShortenerService implements UrlShortenerService {

    private final IdSpace idSpace;
    private final ForwardIndex forwardIndex;
    private final ReverseIndex reverseIndex;
    private final Base64Codec codec;
    private final UrlValidator validator;
    private final UrlNormalizer normalizer;

    public DefaultUrlShortenerService(IdSpace idSpace,
                                      ForwardIndex forwardIndex,
                                      ReverseIndex reverseIndex,
                                      Base64Codec codec,
                                      UrlValidator validator,
                                      UrlNormalizer normalizer) {
        this.idSpace = idSpace;
        this.forwardIndex = forwardIndex;
        this.reverseIndex = reverseIndex;
        this.codec = codec;
        this.validator = validator;
        this.normalizer = normalizer;
    }

    @Override
    public String shorten(String longUrl) throws InvalidUrlException, CapacityExceededException {
        // Validate input URL
        validator.validate(longUrl);
        // Apply normalization (if configured)
        String normalized = normalizer.normalize(longUrl);
        // Check if this URL has been shortened before
        Optional<Long> existingId = reverseIndex.getId(normalized);
        if (existingId.isPresent()) {
            return codec.encode(existingId.get());
        }
        // Allocate a new ID and record the mapping
        long id = idSpace.allocate();
        // Write to forward and reverse indexes
        forwardIndex.put(id, normalized);
        long actualId = reverseIndex.putIfAbsent(normalized, id);
        // If another thread stored an ID simultaneously, use that one
        if (actualId != id) {
            // Remove the unused entry from forward index to avoid leaks
            // Note: ForwardIndex implementation may keep the slot null or reused. We cannot delete from segmented array easily.
            return codec.encode(actualId);
        }
        return codec.encode(id);
    }

    @Override
    public Optional<String> resolve(String code) {
        // Validate code format and decode
        if (!codec.isValidCode(code)) {
            return Optional.empty();
        }
        long id;
        try {
            id = codec.decode(code);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return forwardIndex.get(id);
    }
}