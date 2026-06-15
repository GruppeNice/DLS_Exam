package com.dlsexam.userservice.security;

import com.dlsexam.userservice.config.GoogleOAuthProperties;
import com.dlsexam.userservice.dto.AuthDtos.AuthResponse;
import com.dlsexam.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(prefix = "app.oauth.google", name = "enabled", havingValue = "true")
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final GoogleOAuthProperties oauthProperties;

    public OAuth2LoginSuccessHandler(UserService userService, GoogleOAuthProperties oauthProperties) {
        this.userService = userService;
        this.oauthProperties = oauthProperties;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        String displayName = principal.getAttribute("name");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account did not return an email");
            return;
        }

        AuthResponse auth = userService.loginOrRegisterFromOAuth(email, displayName);

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        String redirectUrl = UriComponentsBuilder.fromUriString(oauthProperties.frontendRedirectUrl())
            .queryParam("token", auth.accessToken())
            .queryParam("expiresIn", auth.expiresInSeconds())
            .queryParam("email", URLEncoder.encode(auth.user().email(), StandardCharsets.UTF_8))
            .build(true)
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
