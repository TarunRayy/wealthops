package com.wealthops.registration.dto;

import com.wealthops.registration.entity.Role;

import java.time.LocalDateTime;

/** Response after successful registration. Never includes password/hash. */
public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Long branchId;
    private LocalDateTime createdAt;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String fullName, String email, Role role, Long branchId, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.branchId = branchId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}