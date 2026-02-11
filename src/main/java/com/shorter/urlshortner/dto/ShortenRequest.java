package com.shorter.urlshortner.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
    @NotBlank(message = "Long URL is required")
    @URL(message = "Invalid URL format")
    @Size(max = 2048, message = "Long URL must be less than 2048 characters")
    String longUrl,

    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Custom code must contain only letters and numbers")
    @Size(max = 32, message = "Custom code must be at most 32 characters")
    String customCode,

    @NotBlank(message = "User ID is required")
    String userId,

    @Positive(message = "TTL days must be positive")
    Integer ttlDays
) {}
