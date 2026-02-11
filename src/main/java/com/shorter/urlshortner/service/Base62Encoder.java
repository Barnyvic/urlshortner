package com.shorter.urlshortner.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom secureRandom = new SecureRandom();

    public String encode(long value) {
        StringBuilder encoded = new StringBuilder();
        long v = value;
        if (v == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }
        if (v < 0) {
            v = -v;
        }
        while (v > 0) {
            encoded.insert(0, BASE62_CHARS.charAt((int) (v % 62)));
            v /= 62;
        }
        return encoded.toString();
    }
    
    public long decode(String encoded) {
        long decoded = 0;
        for (int i = 0; i < encoded.length(); i++) {
            decoded = decoded * 62 + BASE62_CHARS.indexOf(encoded.charAt(i));
        }
        return decoded;
    }

    public String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(BASE62_CHARS.length());
            sb.append(BASE62_CHARS.charAt(idx));
        }
        return sb.toString();
    }
}
