package com.dlsexam.userservice.security;

import com.dlsexam.userservice.config.GoogleOAuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(prefix = "app.oauth.google", name = "enabled", havingValue = "true")
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    private final GoogleOAuthProperties oauthProperties;

    public OAuth2LoginFailureHandler(GoogleOAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        log.warn("Google OAuth login failed: {}", exception.getMessage());
        String loginUrl = oauthProperties.frontendRedirectUrl().replace("/oauth/callback", "/login");
        String redirectUrl = UriComponentsBuilder.fromUriString(loginUrl)
            .queryParam("error", "Google sign-in failed. Try again or use email/password.")
            .build(true)
            .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
