package com.shorter.urlshortner.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shorter.urlshortner.dto.ShortenRequest;
import com.shorter.urlshortner.dto.ShortenResponse;
import com.shorter.urlshortner.service.ShortUrlService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shorten")
@Validated
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

     @Value("${urlshortener.redirect-type:302}")
    private String redirectType;

    @PostMapping
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request,
        HttpServletRequest req
    ) {
        String baseUrl = baseUrl(req);
        String userId = userId(req);
        ShortenResponse response = shortUrlService.shorten(request, baseUrl, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return shortUrlService.resolve(shortCode)
            .map(shortUrl -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(shortUrl.getLongUrl()));
                return new ResponseEntity<Void>(headers,
                    "301".equals(redirectType) ? HttpStatus.MOVED_PERMANENTLY : HttpStatus.FOUND);
            })
            .orElse(ResponseEntity.status(HttpStatus.GONE).build()); 
    }



    private String baseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        String host = req.getServerName();
        int port = req.getServerPort();

        boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(host);
        if (!isDefaultPort) {
            sb.append(":").append(port);
        }
        return sb.toString();
    }

    private String userId(HttpServletRequest req) {
        String uid = req.getHeader("X-User-Id");
        return uid != null ? uid : req.getRemoteAddr();
    }
}

