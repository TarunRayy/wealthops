package com.wealthops.auth.service.impl;

import com.wealthops.auth.dto.LoginRequest;
import com.wealthops.auth.dto.LoginResponse;
import com.wealthops.auth.security.CustomUserPrincipal;
import com.wealthops.auth.security.JwtUtil;
import com.wealthops.auth.service.AuthService;
import com.wealthops.exception.InvalidCredentialsException;
import com.wealthops.registration.entity.User;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        CustomUserPrincipal principal;
        try {
            // Delegates to CustomUserDetailsService (loads the user) +
            // the PasswordEncoder bean (checks the BCrypt hash) — both
            // already wired for us via DaoAuthenticationProvider
            // auto-configuration, nothing extra to set up here.
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword())
            );
            principal = (CustomUserPrincipal) authentication.getPrincipal();

        } catch (AuthenticationException ex) {
            // Covers bad password, unknown email, AND a deactivated
            // account (DisabledException, since CustomUserPrincipal
            // .isEnabled() = user.isActive()) — all collapsed into the
            // same generic message so none of that is distinguishable
            // from the outside.
            throw new InvalidCredentialsException();
        }

        User user = principal.getUser();
        String token = jwtUtil.generateToken(user);

        return new LoginResponse(
                token,
                jwtUtil.getExpirationMs(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getBranch() != null ? user.getBranch().getId() : null
        );
    }
}
