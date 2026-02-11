package com.shorter.urlshortner.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public final class ReservedCodes {

    private static final Set<String> RESERVED_CODES = new HashSet<>(Arrays.asList(
        "admin", "api", "auth", "blog", "contact", "dashboard", "docs", "example", "faq", "features", "feedback", "help", "home", "info", "login", "logout", "register", "search", "settings", "support", "terms", "tos", "user", "users", "welcome"
    ));

    public static boolean isReservedCode(String code) {
        return RESERVED_CODES.contains(code.toLowerCase());
    }
}
