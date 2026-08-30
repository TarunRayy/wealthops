package com.wealthops.auth.dto;

import com.wealthops.registration.entity.Role;

public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresInMs;

    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private Long branchId;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, long expiresInMs, Long userId, String fullName,
                          String email, Role role, Long branchId) {
        this.accessToken = accessToken;
        this.expiresInMs = expiresInMs;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.branchId = branchId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Long getBranchId() {
        return branchId;
    }
}
