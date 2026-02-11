package com.shorter.urlshortner.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shorter.urlshortner.repository.ShortUrlRepository;

import jakarta.transaction.Transactional;

@Component
public class ExpirationCleanupJob {

    private final ShortUrlRepository shortUrlRepository;

    public ExpirationCleanupJob(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpired() {
        shortUrlRepository.deleteExpired(Instant.now());
    }
}
