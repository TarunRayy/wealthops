package com.wealthops.auth.security;

import com.wealthops.registration.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wraps our domain User entity so Spring Security can work with it,
 * without coupling User itself to the Spring Security API.
 */
public class CustomUserPrincipal implements UserDetails {

    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    /** Access the underlying domain entity when needed (e.g. in controllers). */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix is Spring Security's convention for role-based authorities
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // we log in with email, not a separate username field
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // no expiry concept in WealthOps yet
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // no lockout concept yet
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // no password expiry policy yet
    }

    @Override
    public boolean isEnabled() {
        return user.isActive(); // deactivated users can't authenticate
    }
}