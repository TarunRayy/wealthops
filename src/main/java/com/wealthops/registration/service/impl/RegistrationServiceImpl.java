package com.wealthops.registration.service.impl;

import com.wealthops.branch.entity.Branch;
import com.wealthops.branch.repository.BranchRepository;
import com.wealthops.exception.BranchNotFoundException;
import com.wealthops.exception.DuplicateEmailException;
import com.wealthops.exception.InvalidRoleAssignmentException;
import com.wealthops.registration.dto.RegisterClientRequest;
import com.wealthops.registration.dto.RegisterResponse;
import com.wealthops.registration.dto.RegisterStaffRequest;
import com.wealthops.registration.entity.Role;
import com.wealthops.registration.entity.User;
import com.wealthops.registration.repository.UserRepository;
import com.wealthops.registration.service.RegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Set<Role> ASSIGNABLE_STAFF_ROLES =
            EnumSet.of(Role.BRANCH_MANAGER, Role.RELATIONSHIP_MANAGER, Role.COMPLIANCE_OFFICER);

    private static final Set<Role> BRANCH_REQUIRED_ROLES =
            EnumSet.of(Role.BRANCH_MANAGER, Role.RELATIONSHIP_MANAGER);

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationServiceImpl(UserRepository userRepository,
                                   BranchRepository branchRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public RegisterResponse registerClient(RegisterClientRequest request) {
        assertEmailAvailable(request.getEmail());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RegisterResponse registerStaff(RegisterStaffRequest request) {
        assertEmailAvailable(request.getEmail());

        Role role = request.getRole();
        if (!ASSIGNABLE_STAFF_ROLES.contains(role)) {
            throw new InvalidRoleAssignmentException(
                    "Role '" + role + "' cannot be created via staff registration. " +
                            "Allowed roles: " + ASSIGNABLE_STAFF_ROLES);
        }

        Branch branch = null;
        if (BRANCH_REQUIRED_ROLES.contains(role)) {
            if (request.getBranchId() == null) {
                throw new InvalidRoleAssignmentException(
                        "branchId is required when creating a " + role + " account");
            }
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException(request.getBranchId()));
        } else if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException(request.getBranchId()));
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setBranch(branch);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private void assertEmailAvailable(String email) {
        String normalized = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalized)) {
            throw new DuplicateEmailException(normalized);
        }
    }

    private RegisterResponse toResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getBranch() != null ? user.getBranch().getId() : null,
                user.getCreatedAt()
        );
    }
}