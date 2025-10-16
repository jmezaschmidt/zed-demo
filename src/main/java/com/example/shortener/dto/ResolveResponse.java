package com.example.shortener.dto;

/**
 * Response payload for resolving a short code. Contains the original long URL.
 */
public class ResolveResponse {
    private final String longUrl;

    public ResolveResponse(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }
}