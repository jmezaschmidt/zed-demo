package com.example.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for shortening a URL.
 */
public class ShortenRequest {

    @NotNull
    @NotBlank
    private String longUrl;

    public ShortenRequest() {
        // default constructor for JSON deserialization
    }

    public ShortenRequest(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
}