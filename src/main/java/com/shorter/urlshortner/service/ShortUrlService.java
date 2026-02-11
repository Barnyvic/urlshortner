package com.shorter.urlshortner.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.shorter.urlshortner.config.ReservedCodes;
import com.shorter.urlshortner.dto.ShortenRequest;
import com.shorter.urlshortner.dto.ShortenResponse;
import com.shorter.urlshortner.entity.ShortUrl;
import com.shorter.urlshortner.repository.ShortUrlRepository;

import jakarta.transaction.Transactional;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final Base62Encoder base62Encoder;
    private final UrlValidator urlValidator;
    private final int shortCodeLength;
    private final int defaultTtlDays;

    public ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            Base62Encoder base62Encoder,
            UrlValidator urlValidator,
            @Value("${urlshortener.short-code-length:7}") int shortCodeLength,
            @Value("${urlshortener.default-ttl-days:365}") int defaultTtlDays) {
        this.shortUrlRepository = shortUrlRepository;
        this.base62Encoder = base62Encoder;
        this.urlValidator = urlValidator;
        this.shortCodeLength = shortCodeLength;
        this.defaultTtlDays = defaultTtlDays;
    }

    @Transactional
    public ShortenResponse shorten(ShortenRequest request, String baseUrl, String userId) {
        urlValidator.validate(request.longUrl());

        String shortCode;
        boolean customCode = false;

        if (request.customCode() != null && !request.customCode().isBlank()) {
            shortCode = request.customCode().trim();
            if (!urlValidator.isValidCustomCode(shortCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid custom code: " + shortCode);
            }
            if (ReservedCodes.isReservedCode(shortCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom code is reserved: " + shortCode);
            }
            if (shortUrlRepository.existsByShortCodeIgnoreCase(shortCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom code is already in use: " + shortCode);
            }
            customCode = true;
        } else {
            shortCode = generateRandomShortCode();
        }

        int ttlDays = (request.ttlDays() != null && request.ttlDays() > 0) ? request.ttlDays() : defaultTtlDays;
        Instant expiresAt = Instant.now().plus(ttlDays, ChronoUnit.DAYS);

        ShortUrl entity = ShortUrl.builder()
                .shortCode(shortCode)
                .longUrl(request.longUrl())
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .customCode(customCode)
                .userId(userId)
                .build();

        shortUrlRepository.save(entity);

        String fullShortUrl = baseUrl + "/" + shortCode;
        return new ShortenResponse(shortCode, request.longUrl(), fullShortUrl, entity.getCreatedAt(), expiresAt);
    }


    public Optional<ShortUrl> resolve(String shortCode) {
        return shortUrlRepository.findByShortCodeIgnoreCase(shortCode);
    }


    private String generateRandomShortCode() {
        return base62Encoder.randomCode(shortCodeLength);
    }
}
