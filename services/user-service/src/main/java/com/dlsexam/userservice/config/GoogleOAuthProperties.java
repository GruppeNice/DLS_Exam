package com.dlsexam.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth.google")
public record GoogleOAuthProperties(
    boolean enabled,
    String frontendRedirectUrl
) {
}
