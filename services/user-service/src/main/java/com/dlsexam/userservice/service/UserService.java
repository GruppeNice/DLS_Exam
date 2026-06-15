package com.dlsexam.userservice.service;

import com.dlsexam.userservice.domain.AccountStatus;
import com.dlsexam.userservice.domain.User;
import com.dlsexam.userservice.dto.AuthDtos.AuthResponse;
import com.dlsexam.userservice.dto.AuthDtos.LoginRequest;
import com.dlsexam.userservice.dto.AuthDtos.RegisterRequest;
import com.dlsexam.userservice.dto.AuthDtos.UserProfileResponse;
import com.dlsexam.userservice.messaging.UserEventPublisher;
import com.dlsexam.userservice.repository.UserRepository;
import com.dlsexam.userservice.security.JwtService;
import com.dlsexam.userservice.security.UserPrincipal;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserEventPublisher eventPublisher;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of("USER"));
        User saved = userRepository.save(user);
        eventPublisher.userRegistered(saved.getId(), saved.getEmail());
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
        return buildAuthResponse(user);
    }

    public UserProfileResponse me(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toProfile(user);
    }

    @Transactional
    public UserProfileResponse suspend(UUID userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(AccountStatus.SUSPENDED);
        User saved = userRepository.save(user);
        eventPublisher.userSuspended(saved.getId(), saved.getEmail(), reason);
        return toProfile(saved);
    }

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
        eventPublisher.userDeleted(user.getId(), user.getEmail());
    }

    public void requestPasswordReset(String email) {
        // Intentionally noop for now. In real flow, this emits a reset request event
        // and/or sends an email through Engagement Service.
        userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional
    public AuthResponse loginOrRegisterFromOAuth(String email, String displayName) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(normalizedEmail);
            newUser.setDisplayName(
                displayName != null && !displayName.isBlank() ? displayName.trim() : normalizedEmail
            );
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setStatus(AccountStatus.ACTIVE);
            newUser.setRoles(Set.of("USER"));
            User saved = userRepository.save(newUser);
            eventPublisher.userRegistered(saved.getId(), saved.getEmail());
            return saved;
        });
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRoles());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), toProfile(user));
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getStatus(),
            user.getRoles(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
