package com.example.shortener.dto;

/**
 * Response payload for shortening a URL. Contains the generated short code.
 */
public class ShortenResponse {
    private final String shortCode;

    public ShortenResponse(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}