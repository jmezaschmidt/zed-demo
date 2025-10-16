package com.example.shortener.core;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Implementation of {@link Base64Codec} that uses Java's built-in Base64
 * URL encoder/decoder. IDs are restricted to 48 bits and encoded as 6 bytes
 * which produce exactly 8 Base64 characters. No padding characters are used.
 */
public class Base64UrlCodec implements Base64Codec {
    private static final long MAX_ID = (1L << 48) - 1;
    private static final int BYTES_LENGTH = 6; // 48 bits / 8 = 6 bytes
    private static final int CODE_LENGTH = 8;  // 6 bytes -> 8 Base64 chars
    private static final Pattern VALID_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8}$");

    private final Base64.Encoder encoder;
    private final Base64.Decoder decoder;

    public Base64UrlCodec() {
        this.encoder = Base64.getUrlEncoder().withoutPadding();
        this.decoder = Base64.getUrlDecoder();
    }

    @Override
    public String encode(long id) {
        if (id < 0 || id > MAX_ID) {
            throw new IllegalArgumentException("ID out of range: " + id);
        }
        byte[] buffer = new byte[BYTES_LENGTH];
        // big-endian encoding: write lowest 6 bytes of the id into the array
        for (int i = BYTES_LENGTH - 1; i >= 0; i--) {
            buffer[i] = (byte) (id & 0xFF);
            id >>>= 8;
        }
        String encoded = encoder.encodeToString(buffer);
        if (encoded.length() != CODE_LENGTH) {
            // Safety check: should always be 8 characters
            throw new IllegalStateException("Unexpected code length: " + encoded);
        }
        return encoded;
    }

    @Override
    public long decode(String code) {
        if (!isValidCode(code)) {
            throw new IllegalArgumentException("Invalid code format: " + code);
        }
        byte[] bytes = decoder.decode(code);
        if (bytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException(
                    "Decoded byte array length must be " + BYTES_LENGTH + ", but was " + bytes.length);
        }
        long id = 0L;
        for (byte b : bytes) {
            id = (id << 8) | (b & 0xFF);
        }
        return id;
    }

    @Override
    public boolean isValidCode(String code) {
        return code != null && VALID_CODE_PATTERN.matcher(code).matches();
    }
}