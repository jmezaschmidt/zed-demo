package com.example.shortener.controller;

import com.example.shortener.dto.ResolveRequest;
import com.example.shortener.dto.ResolveResponse;
import com.example.shortener.dto.ShortenRequest;
import com.example.shortener.dto.ShortenResponse;
import com.example.shortener.errors.CapacityExceededException;
import com.example.shortener.errors.InvalidUrlException;
import com.example.shortener.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the ShortenerController using Spring Boot's
 * WebMvcTest to test the web layer in isolation.
 */
@WebMvcTest(ShortenerController.class)
class ShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlShortenerService service;

    @Test
    void shorten_withValidUrl_returnsShortCode() throws Exception {
        // Arrange
        String longUrl = "https://example.com/very/long/url";
        String expectedCode = "abc12345";
        when(service.shorten(longUrl)).thenReturn(expectedCode);

        ShortenRequest request = new ShortenRequest(longUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortCode").value(expectedCode));
    }

    @Test
    void shorten_withInvalidUrl_returnsBadRequest() throws Exception {
        // Arrange
        String invalidUrl = "not-a-valid-url";
        String errorMessage = "Invalid URL format";
        when(service.shorten(invalidUrl)).thenThrow(new InvalidUrlException(errorMessage));

        ShortenRequest request = new ShortenRequest(invalidUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void shorten_withBlankUrl_handledByService() throws Exception {
        // Arrange - blank URL gets passed to service which throws exception
        String blankUrl = "";
        when(service.shorten(blankUrl)).thenThrow(new InvalidUrlException("URL must be non-empty"));

        ShortenRequest request = new ShortenRequest(blankUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("URL must be non-empty"));
    }

    @Test
    void shorten_withNullUrl_handledByService() throws Exception {
        // Arrange - null URL gets passed to service which throws exception
        when(service.shorten(null)).thenThrow(new InvalidUrlException("URL must be non-empty"));

        ShortenRequest request = new ShortenRequest(null);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("URL must be non-empty"));
    }

    @Test
    void shorten_whenCapacityExceeded_returnsServiceUnavailable() throws Exception {
        // Arrange
        String longUrl = "https://example.com/test";
        String errorMessage = "No more IDs available";
        when(service.shorten(longUrl)).thenThrow(new CapacityExceededException(errorMessage));

        ShortenRequest request = new ShortenRequest(longUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void resolve_withKnownCode_returnsLongUrl() throws Exception {
        // Arrange
        String shortCode = "abc12345";
        String expectedUrl = "https://example.com/very/long/url";
        when(service.resolve(shortCode)).thenReturn(Optional.of(expectedUrl));

        ResolveRequest request = new ResolveRequest(shortCode);

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.longUrl").value(expectedUrl));
    }

    @Test
    void resolve_withUnknownCode_returnsNotFound() throws Exception {
        // Arrange
        String shortCode = "unknown1";
        when(service.resolve(shortCode)).thenReturn(Optional.empty());

        ResolveRequest request = new ResolveRequest(shortCode);

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolve_withBlankCode_returnsNotFound() throws Exception {
        // Arrange - blank code is passed through and not found
        String blankCode = "";
        when(service.resolve(blankCode)).thenReturn(Optional.empty());

        ResolveRequest request = new ResolveRequest(blankCode);

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolve_withNullCode_returnsNotFound() throws Exception {
        // Arrange - null code is passed through and not found
        when(service.resolve(null)).thenReturn(Optional.empty());

        ResolveRequest request = new ResolveRequest(null);

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shorten_withMalformedJson_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolve_withMalformedJson_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_withMissingContentType_returnsUnsupportedMediaType() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest("https://example.com");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void resolve_withMissingContentType_returnsUnsupportedMediaType() throws Exception {
        // Arrange
        ResolveRequest request = new ResolveRequest("abc12345");

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shorten_withVeryLongUrl_handlesCorrectly() throws Exception {
        // Arrange
        String longUrl = "https://example.com/" + "x".repeat(1000);
        String expectedCode = "longurl1";
        when(service.shorten(longUrl)).thenReturn(expectedCode);

        ShortenRequest request = new ShortenRequest(longUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(expectedCode));
    }

    @Test
    void resolve_withSpecialCharactersInCode_handlesCorrectly() throws Exception {
        // Arrange
        String shortCode = "abc-_123";
        String expectedUrl = "https://example.com/test";
        when(service.resolve(shortCode)).thenReturn(Optional.of(expectedUrl));

        ResolveRequest request = new ResolveRequest(shortCode);

        // Act & Assert
        mockMvc.perform(post("/api/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longUrl").value(expectedUrl));
    }

    @Test
    void shorten_withHttpsUrl_returnsShortCode() throws Exception {
        // Arrange
        String httpsUrl = "https://secure.example.com/page";
        String expectedCode = "secure01";
        when(service.shorten(httpsUrl)).thenReturn(expectedCode);

        ShortenRequest request = new ShortenRequest(httpsUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(expectedCode));
    }

    @Test
    void shorten_withHttpUrl_returnsShortCode() throws Exception {
        // Arrange
        String httpUrl = "http://example.com/page";
        String expectedCode = "http0001";
        when(service.shorten(httpUrl)).thenReturn(expectedCode);

        ShortenRequest request = new ShortenRequest(httpUrl);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(expectedCode));
    }
}

