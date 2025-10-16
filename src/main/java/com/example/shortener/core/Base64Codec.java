package com.example.shortener.core;

/**
 * Codec for encoding and decoding 48-bit IDs into an 8-character Base64 URL-safe
 * string without padding and vice versa. The encode/decode methods should
 * throw {@link IllegalArgumentException} if inputs are invalid (e.g., code
 * format does not match expectations, or id is outside the supported range).
 */
public interface Base64Codec {
    /**
     * Encodes a 48-bit ID to an 8-character Base64 URL-safe string.
     *
     * @param id the ID to encode (0 <= id <= 2^48 - 1)
     * @return an 8-character Base64 URL-safe string without padding
     * @throws IllegalArgumentException if the id is out of range
     */
    String encode(long id);

    /**
     * Decodes an 8-character Base64 URL-safe string back to its 48-bit ID.
     *
     * @param code the 8-character Base64 URL-safe string without padding
     * @return the decoded 48-bit ID
     * @throws IllegalArgumentException if the input is not exactly 8 valid
     *                                  Base64 URL-safe characters
     */
    long decode(String code);

    /**
     * Validates that the given string conforms to the 8-character Base64 URL-safe
     * format expected by this codec.
     *
     * @param code the code to validate
     * @return true if valid, false otherwise
     */
    boolean isValidCode(String code);
}