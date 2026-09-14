package com.wealthops.security;

import com.wealthops.registration.entity.Role;
import com.wealthops.registration.entity.User;
import com.wealthops.registration.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated user (via DB lookup each call)
 * and exposes role/branch info used for scope-based access filtering.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /** Null for SUPER_ADMIN, COMPLIANCE_OFFICER, and CLIENT. */
    public Long getCurrentUserBranchId() {
        User user = getCurrentUser();
        return user.getBranch() != null ? user.getBranch().getId() : null;
    }

    public boolean isAdminOrCompliance() {
        Role role = getCurrentUserRole();
        return role == Role.SUPER_ADMIN || role == Role.COMPLIANCE_OFFICER;
    }
}