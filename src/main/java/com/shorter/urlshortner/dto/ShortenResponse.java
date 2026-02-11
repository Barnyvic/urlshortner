package com.shorter.urlshortner.dto;

import java.time.Instant;

public record ShortenResponse(
    String shortCode,
    String longUrl,
    String shortUrl,
    Instant createdAt,
    Instant expiresAt
) {
} 
