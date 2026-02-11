package com.shorter.urlshortner.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.shorter.urlshortner.service.ShortUrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortUrlService shortUrlService;

    @Value("${urlshortener.redirect-type:302}")
    private String redirectType;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return shortUrlService.resolve(shortCode)
                .map(shortUrl -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(URI.create(shortUrl.getLongUrl()));
                    HttpStatus status = "301".equals(redirectType)
                            ? HttpStatus.MOVED_PERMANENTLY
                            : HttpStatus.FOUND;
                    return new ResponseEntity<Void>(headers, status);
                })
                .orElse(ResponseEntity.status(HttpStatus.GONE).build());
    }
}

