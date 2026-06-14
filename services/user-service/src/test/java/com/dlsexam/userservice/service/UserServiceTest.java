package com.dlsexam.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dlsexam.userservice.domain.AccountStatus;
import com.dlsexam.userservice.domain.User;
import com.dlsexam.userservice.dto.AuthDtos.AuthResponse;
import com.dlsexam.userservice.dto.AuthDtos.LoginRequest;
import com.dlsexam.userservice.dto.AuthDtos.RegisterRequest;
import com.dlsexam.userservice.messaging.UserEventPublisher;
import com.dlsexam.userservice.repository.UserRepository;
import com.dlsexam.userservice.security.JwtService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserEventPublisher eventPublisher;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, authenticationManager, jwtService, eventPublisher);
    }

    @Test
    void loginReturnsTokenForActiveUser() {
        LoginRequest request = new LoginRequest("alice@example.com", "plain-password");
        User user = buildUser(UUID.randomUUID(), "alice@example.com", AccountStatus.ACTIVE, Set.of("USER"));

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user.getId(), user.getEmail(), user.getRoles())).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = userService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
    }

    @Test
    void loginRejectsSuspendedUser() {
        LoginRequest request = new LoginRequest("bob@example.com", "pw");
        User user = buildUser(UUID.randomUUID(), "bob@example.com", AccountStatus.SUSPENDED, Set.of("USER"));
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.login(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getReason()).isEqualTo("Account is not active");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.register(request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    private User buildUser(UUID id, String email, AccountStatus status, Set<String> roles) {
        User user = new User();
        user.setEmail(email);
        user.setStatus(status);
        user.setRoles(roles);

        // Keep object close to a persisted entity shape for auth-response generation.
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set id for test fixture", e);
        }

        return user;
    }
}

