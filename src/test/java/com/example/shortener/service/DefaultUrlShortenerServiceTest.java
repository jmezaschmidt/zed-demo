package com.example.shortener.service;

import com.example.shortener.core.AtomicIdSpace;
import com.example.shortener.core.Base64UrlCodec;
import com.example.shortener.core.ConcurrentReverseIndex;
import com.example.shortener.core.DefaultUrlValidator;
import com.example.shortener.core.NoOpUrlNormalizer;
import com.example.shortener.core.SegmentedForwardIndex;
import com.example.shortener.errors.CapacityExceededException;
import com.example.shortener.errors.InvalidUrlException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link DefaultUrlShortenerService}. These tests verify basic
 * shorten/resolve functionality, idempotency, unknown codes and validation.
 */
public class DefaultUrlShortenerServiceTest {

    private UrlShortenerService service;

    @BeforeEach
    public void setUp() {
        service = new DefaultUrlShortenerService(
                new AtomicIdSpace(),
                new SegmentedForwardIndex(20),
                new ConcurrentReverseIndex(),
                new Base64UrlCodec(),
                new DefaultUrlValidator(2048, Set.of("http", "https")),
                new NoOpUrlNormalizer());
    }

    @Test
    public void testShortenAndResolve() {
        String url = "http://example.com";
        String code = service.shorten(url);
        assertThat(code).hasSize(8);
        Optional<String> resolved = service.resolve(code);
        assertThat(resolved).contains(url);
    }

    @Test
    public void testIdempotentShorten() {
        String url = "https://example.org/path";
        String code1 = service.shorten(url);
        String code2 = service.shorten(url);
        assertThat(code1).isEqualTo(code2);
    }

    @Test
    public void testUnknownCodeReturnsEmpty() {
        Optional<String> resolved = service.resolve("aaaaaaaa");
        assertThat(resolved).isEmpty();
    }

    @Test
    public void testInvalidCodeReturnsEmpty() {
        // Contains an invalid character '!'
        Optional<String> resolved = service.resolve("!!!!@@@@");
        assertThat(resolved).isEmpty();
    }

    @Test
    public void testInvalidUrlThrowsException() {
        assertThatThrownBy(() -> service.shorten("ftp://example.com"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> service.shorten(""))
                .isInstanceOf(InvalidUrlException.class);
    }
}