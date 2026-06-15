package com.dlsexam.userservice.controller;

import com.dlsexam.userservice.config.GoogleOAuthProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth/google")
public class OAuthController {

    private final GoogleOAuthProperties oauthProperties;

    public OAuthController(GoogleOAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", oauthProperties.enabled());
        body.put("provider", "google");
        if (oauthProperties.enabled()) {
            body.put("authorizationPath", "/oauth2/authorization/google");
            body.put("callbackPath", "/login/oauth2/code/google");
        }
        return body;
    }
}
