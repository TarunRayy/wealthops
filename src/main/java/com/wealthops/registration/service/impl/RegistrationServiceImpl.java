package com.wealthops.registration.service.impl;

import com.wealthops.branch.entity.Branch;
import com.wealthops.audit.entity.AuditAction;
import com.wealthops.audit.service.AuditLogService;
import com.wealthops.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Set<Role> ASSIGNABLE_STAFF_ROLES =
            EnumSet.of(Role.BRANCH_MANAGER, Role.RELATIONSHIP_MANAGER, Role.COMPLIANCE_OFFICER);

    private static final Set<Role> BRANCH_REQUIRED_ROLES =
            EnumSet.of(Role.BRANCH_MANAGER, Role.RELATIONSHIP_MANAGER);

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public RegistrationServiceImpl(UserRepository userRepository,
                                   BranchRepository branchRepository,
                                   PasswordEncoder passwordEncoder,
                                   AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
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
    public RegisterResponse registerStaff(RegisterStaffRequest request, String requesterEmail) {
        assertEmailAvailable(request.getEmail());


        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        Role role = request.getRole();
        if (!ASSIGNABLE_STAFF_ROLES.contains(role)) {
            throw new InvalidRoleAssignmentException(
                    "Role '" + role + "' cannot be created via staff registration. " +
                            "Allowed roles: " + ASSIGNABLE_STAFF_ROLES);
        }

        if (requester.getRole() == Role.BRANCH_MANAGER) {
            if (requester.getBranch() == null
                    || request.getBranchId() == null
                    || !requester.getBranch().getId().equals(request.getBranchId())) {
                throw new AccessDeniedException("You can only create staff within your own branch");
            }
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

        auditLogService.log(requester.getEmail(), requester.getRole().name(),
                AuditAction.STAFF_REGISTERED, "User", saved.getId(),
                null, "Registered " + saved.getRole() + ": " + saved.getEmail());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegisterResponse> getAllStaff(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        List<User> staff = requester.getRole() == Role.BRANCH_MANAGER
                ? userRepository.findByBranchIdAndRoleIn(requester.getBranch().getId(), ASSIGNABLE_STAFF_ROLES)
                : userRepository.findByRoleIn(ASSIGNABLE_STAFF_ROLES);

        return staff.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegisterResponse> getStaffByBranch(Long branchId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() == Role.BRANCH_MANAGER
                && (requester.getBranch() == null || !requester.getBranch().getId().equals(branchId))) {
            throw new ResourceNotFoundException("Branch not found with id: " + branchId);
        }

        return userRepository.findByBranchIdAndRoleIn(branchId, ASSIGNABLE_STAFF_ROLES)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegisterResponse> getRelationshipManagers(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        List<User> rms = requester.getRole() == Role.BRANCH_MANAGER
                ? userRepository.findByBranchIdAndRoleIn(requester.getBranch().getId(), Set.of(Role.RELATIONSHIP_MANAGER))
                : userRepository.findByRole(Role.RELATIONSHIP_MANAGER);

        return rms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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