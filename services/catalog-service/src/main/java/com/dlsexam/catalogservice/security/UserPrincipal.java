package com.dlsexam.catalogservice.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserPrincipal implements org.springframework.security.core.userdetails.UserDetails {

    private final UUID userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID userId, String email, Collection<String> roles) {
        this.userId = userId;
        this.email = email;
        this.authorities = roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
