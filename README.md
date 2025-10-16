# Runtime‑Only URL Shortener

Testing a commit

This project is a minimal in‑memory URL shortener built with **Java 17** and **Spring Boot 3**.  It encodes sequential 48‑bit identifiers into 8‑character Base64URL strings (e.g. `abcD1234`) and maps them back to the original URLs.  All mappings live in memory; if the application is restarted the codes are recreated.

## Features

- **In‑memory runtime storage:** no external database or persistent store required.
- **Deterministic within a run:** shortening the same (normalized) URL returns the same code until the service restarts.
- **O(1) lookup:** the resolver uses a segmented array to guarantee constant‑time `code → URL` lookups.
- **Large ID space:** a 48‑bit ID yields roughly 280 trillion unique codes.
- **HTTP/HTTPS only:** only `http` and `https` schemes are accepted; maximum URL length is 2048 characters.
- **Optional normalization:** URL normalization is disabled by default but can be extended (e.g. trimming, lowercasing host, sorting query params).

## Prerequisites

To build and run the project you need:

- **Java 17** or later installed on your system.
- **Maven 3.8.x** or later.

## Building and Running

Clone or download the repository, then from the project root (`shortener`) run:

```bash
# Build the jar
mvn clean package

# Run the application
java -jar target/shortener-0.0.1-SNAPSHOT.jar
```

Alternatively, you can run the application directly via Spring Boot:

```bash
mvn spring-boot:run
```

By default the application starts on port `8080`.  You can override this by passing `--server.port=<port>` to `java -jar` or via the Maven run command.

## API Endpoints

Both endpoints are exposed under the `/api` base path and accept/produce JSON.

### `POST /api/shorten`

Shortens a valid URL into an 8‑character code.

**Request body:**

```json
{
  "longUrl": "https://example.com/path"
}
```

**Response body:**

```json
{
  "shortCode": "abcD1234"
}
```

If the URL is invalid (null, empty, unsupported scheme or too long) the service returns **HTTP 400 Bad Request** with a message.  If the 48‑bit ID space is exhausted (unlikely), it returns **HTTP 503 Service Unavailable**.

### `POST /api/resolve`

Resolves a previously generated code back to its original URL.

**Request body:**

```json
{
  "shortCode": "abcD1234"
}
```

**Response body:**

```json
{
  "longUrl": "https://example.com/path"
}
```

If the code is unknown or does not conform to the expected format (`^[A-Za-z0-9_-]{8}$`), the service responds with **HTTP 404 Not Found**.

## Running Tests

The project includes comprehensive unit tests using **JUnit Jupiter**, **Mockito**, and **AssertJ**.  To execute them, run:

```bash
mvn test
```

## Internal Design Overview

The service is designed to guarantee O(1) lookups while remaining thread‑safe and deterministic within a single run:

- **ID generation:** A `AtomicIdSpace` allocates sequential 48‑bit IDs (`0 … 2^48−1`).  If the counter overflows, a `CapacityExceededException` is thrown.
- **Encoding:** IDs are encoded into 6 bytes (big‑endian) then Base64URL‑encoded using Java’s `Base64.getUrlEncoder().withoutPadding()`.  Six bytes of input always yield eight Base64 characters.
- **Forward index:** A segmented array (`SegmentedForwardIndex`) stores the mapping `id → url`.  The segment size is `2^20` by default (≈1 million entries).  Looking up a code involves decoding it to an ID and then performing a constant‑time array index lookup.
- **Reverse index:** A `ConcurrentHashMap` maintains `url → id` mappings to ensure idempotency for repeated shorten operations.  It is used only during shortening and does not affect resolve‑time performance.
- **Validation:** `DefaultUrlValidator` ensures the URL is non‑blank, under a configurable length, and uses an allowed scheme.  Malformed or unsupported URLs result in an `InvalidUrlException` and an HTTP 400 response.
- **Normalization:** The default `NoOpUrlNormalizer` returns the URL unchanged.  You can provide an alternative implementation to canonicalize URLs if needed.

## Extending the Service

While the current implementation is intentionally simple, it can serve as a foundation for more advanced features.  Potential extensions include:

- Persisting mappings to a database for durability.
- Adding a REST layer for custom aliases and deletions.
- Implementing configurable normalization rules (e.g. lowercasing the host, sorting query parameters, removing default ports).
- Supporting expiration or TTL for codes.
- Integrating metrics and rate‑limiting to guard against abuse.

Feel free to adapt the project to your needs and contribute improvements!