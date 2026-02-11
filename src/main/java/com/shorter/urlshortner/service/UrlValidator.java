package com.shorter.urlshortner.service;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlValidator {
    private final int maxUrlLength;
    private final Set<String> blockedRedirectHosts;

    public UrlValidator(
            @Value("${urlshortener.max-long-url-length}") int maxUrlLength,
            @Value("${urlshortener.blocked-redirect-hosts:localhost,127.0.0.1}") String blockedRedirectHosts) {
        this.maxUrlLength = maxUrlLength;
        this.blockedRedirectHosts = Arrays.stream(blockedRedirectHosts.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public void validate(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }
        if (url.length() > maxUrlLength) {
            throw new IllegalArgumentException("URL is too long. Maximum length is " + maxUrlLength + " characters");
        }

        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid URL scheme. Must be http or https");
            }

            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("URL must have a valid host");
            }

            rejectIfOpenRedirectTarget(host);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + e.getMessage());
        }
    }

    private void rejectIfOpenRedirectTarget(String host) {
        String hostLower = host.toLowerCase();

        if (blockedRedirectHosts.contains(hostLower)) {
            throw new IllegalArgumentException("Redirects to this host are not allowed for security reasons");
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress()) {
                throw new IllegalArgumentException("Redirects to loopback addresses are not allowed");
            }
            if (address.isLinkLocalAddress()) {
                throw new IllegalArgumentException("Redirects to link-local addresses are not allowed");
            }
            if (address.isSiteLocalAddress()) {
                throw new IllegalArgumentException("Redirects to private network addresses are not allowed");
            }
        } catch (java.net.UnknownHostException e) {
            
        }
    }

    public boolean isValidCustomCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return code.matches("^[a-zA-Z0-9]{1,32}$");
    }
}
