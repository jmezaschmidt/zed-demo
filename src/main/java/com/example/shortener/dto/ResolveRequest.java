package com.example.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for resolving a shortened code.
 */
public class ResolveRequest {

    @NotNull
    @NotBlank
    private String shortCode;

    public ResolveRequest() {
        // default constructor for JSON deserialization
    }

    public ResolveRequest(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
}