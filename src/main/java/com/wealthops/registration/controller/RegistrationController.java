package com.wealthops.registration.controller;

import com.wealthops.registration.dto.RegisterClientRequest;
import org.springframework.security.core.Authentication;
import com.wealthops.registration.dto.RegisterResponse;
import com.wealthops.registration.dto.RegisterStaffRequest;
import com.wealthops.registration.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** Public self-service registration for a Client. No auth required. */
    @PostMapping("/client")
    public ResponseEntity<RegisterResponse> registerClient(@Valid @RequestBody RegisterClientRequest request) {
        RegisterResponse response = registrationService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Staff account creation (Branch Manager / RM / Compliance Officer).
     *
     * URL-level rule for /api/v1/register/** is permitAll (so
     * self-registration at /client stays public), but @PreAuthorize
     * here overrides that at the method level: an anonymous caller (no
     * token) or one authenticated as anything other than SUPER_ADMIN /
     * BRANCH_MANAGER gets a 403 via RestAccessDeniedHandler before this
     * method body ever runs.
     */
    @PostMapping("/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<RegisterResponse> registerStaff(@Valid @RequestBody RegisterStaffRequest request,
                                                          Authentication authentication) {
        RegisterResponse response = registrationService.registerStaff(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<java.util.List<RegisterResponse>> getStaff(@RequestParam(required = false) Long branchId,
                                                                     Authentication authentication) {
        if (branchId != null) {
            return ResponseEntity.ok(registrationService.getStaffByBranch(branchId, authentication.getName()));
        }
        return ResponseEntity.ok(registrationService.getAllStaff(authentication.getName()));
    }

    @GetMapping("/rms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<java.util.List<RegisterResponse>> getRelationshipManagers(Authentication authentication) {
        return ResponseEntity.ok(registrationService.getRelationshipManagers(authentication.getName()));
    }
}
