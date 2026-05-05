package com.dlsexam.userservice.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    @GetMapping("/google/status")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> googleOAuthStatus() {
        return Map.of(
            "provider", "google",
            "status", "configured-by-properties",
            "note", "Use spring.security.oauth2.client.registration.google.* env vars to enable login."
        );
    }
}
