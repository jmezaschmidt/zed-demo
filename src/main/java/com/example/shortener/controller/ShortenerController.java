package com.example.shortener.controller;

import com.example.shortener.dto.ResolveRequest;
import com.example.shortener.dto.ResolveResponse;
import com.example.shortener.dto.ShortenRequest;
import com.example.shortener.dto.ShortenResponse;
import com.example.shortener.errors.CapacityExceededException;
import com.example.shortener.errors.InvalidUrlException;
import com.example.shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller exposing endpoints for shortening and resolving URLs. Both
 * operations are exposed as POST to accept a JSON payload describing the
 * input.
 */
@RestController
@RequestMapping("/api")
@Validated
public class ShortenerController {

    private final UrlShortenerService service;

    public ShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    /**
     * Accepts a long URL in the request body and returns a short code.
     *
     * @param request request containing the longUrl to shorten
     * @return ShortenResponse with the generated short code
     */
    @PostMapping(path = "/shorten", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        String code = service.shorten(request.getLongUrl());
        return ResponseEntity.ok(new ShortenResponse(code));
    }

    /**
     * Accepts a short code in the request body and returns the original URL if
     * known. Returns 404 Not Found if the code is unknown or invalid.
     *
     * @param request request containing the shortCode to resolve
     * @return ResolveResponse with the original URL, or 404 if not found
     */
    @PostMapping(path = "/resolve", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResolveResponse> resolve(@Valid @RequestBody ResolveRequest request) {
        Optional<String> longUrl = service.resolve(request.getShortCode());
        return longUrl
                .map(url -> ResponseEntity.ok(new ResolveResponse(url)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Handle InvalidUrlException and return HTTP 400 Bad Request with the error
     * message. This prevents stack traces from leaking to clients.
     */
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<String> handleInvalidUrl(InvalidUrlException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handle CapacityExceededException and return HTTP 503 Service Unavailable
     * indicating that no further IDs can be allocated.
     */
    @ExceptionHandler(CapacityExceededException.class)
    public ResponseEntity<String> handleCapacityExceeded(CapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
