package com.shorter.urlshortner.config;

import java.time.Instant;
import java.time.Duration;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory sliding window rate limiter.
 *
 * Limits each client (by remote IP) to N requests per rolling window.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final int maxRequests;
    private final Duration window;
    private final Map<String, Deque<Instant>> requestTimestamps = new ConcurrentHashMap<>();

    public RateLimitInterceptor(
            @Value("${urlshortener.rate-limit-requests-per-minute:60}") int maxRequestsPerMinute) {
        this.maxRequests = maxRequestsPerMinute;
        this.window = Duration.ofMinutes(1);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = resolveClientKey(request);
        Instant now = Instant.now();

        Deque<Instant> timestamps = requestTimestamps.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            Instant cutoff = now.minus(window);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxRequests) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
            }

            timestamps.addLast(now);
        }

        return true;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return (commaIndex > 0 ? forwarded.substring(0, commaIndex) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }
}
