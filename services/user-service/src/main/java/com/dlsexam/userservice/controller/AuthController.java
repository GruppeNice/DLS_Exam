package com.dlsexam.userservice.controller;

import com.dlsexam.userservice.dto.AuthDtos.AuthResponse;
import com.dlsexam.userservice.dto.AuthDtos.LoginRequest;
import com.dlsexam.userservice.dto.AuthDtos.PasswordResetRequest;
import com.dlsexam.userservice.dto.AuthDtos.RegisterRequest;
import com.dlsexam.userservice.dto.AuthDtos.UserProfileResponse;
import com.dlsexam.userservice.security.UserPrincipal;
import com.dlsexam.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.me(principal);
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        userService.requestPasswordReset(request.email());
    }
}
