package com.example.shortener.config;

import com.example.shortener.core.AtomicIdSpace;
import com.example.shortener.core.Base64Codec;
import com.example.shortener.core.Base64UrlCodec;
import com.example.shortener.core.ConcurrentReverseIndex;
import com.example.shortener.core.DefaultUrlValidator;
import com.example.shortener.core.ForwardIndex;
import com.example.shortener.core.IdSpace;
import com.example.shortener.core.NoOpUrlNormalizer;
import com.example.shortener.core.ReverseIndex;
import com.example.shortener.core.SegmentedForwardIndex;
import com.example.shortener.core.UrlNormalizer;
import com.example.shortener.core.UrlValidator;
import com.example.shortener.service.DefaultUrlShortenerService;
import com.example.shortener.service.UrlShortenerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Spring configuration that wires together the URL shortener service and its
 * supporting components. All beans are singleton-scoped by default.
 */
@Configuration
public class ShortenerConfig {

    @Bean
    public IdSpace idSpace() {
        return new AtomicIdSpace();
    }

    @Bean
    public ForwardIndex forwardIndex() {
        // Use default segment size (1<<20). This yields ~1M entries per segment.
        return new SegmentedForwardIndex();
    }

    @Bean
    public ReverseIndex reverseIndex() {
        return new ConcurrentReverseIndex();
    }

    @Bean
    public Base64Codec base64Codec() {
        return new Base64UrlCodec();
    }

    @Bean
    public UrlValidator urlValidator() {
        return new DefaultUrlValidator(2048, Set.of("http", "https"));
    }

    @Bean
    public UrlNormalizer urlNormalizer() {
        // No-op normalization by default; can be replaced with custom implementation
        return new NoOpUrlNormalizer();
    }

    @Bean
    public UrlShortenerService urlShortenerService(IdSpace idSpace,
                                                   ForwardIndex forwardIndex,
                                                   ReverseIndex reverseIndex,
                                                   Base64Codec codec,
                                                   UrlValidator validator,
                                                   UrlNormalizer normalizer) {
        return new DefaultUrlShortenerService(idSpace, forwardIndex, reverseIndex, codec, validator, normalizer);
    }
}