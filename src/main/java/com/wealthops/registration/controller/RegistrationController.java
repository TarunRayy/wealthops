package com.wealthops.registration.controller;

import com.wealthops.registration.dto.RegisterClientRequest;
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
     *
     * TODO (Phase 5 - scope-based access): a Branch Manager should only
     * be able to create staff within their OWN branch. That's a
     * data-level check this annotation can't express — it belongs in
     * the service layer, comparing the caller's branchId (from the JWT)
     * against request.getBranchId().
     */
    @PostMapping("/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<RegisterResponse> registerStaff(@Valid @RequestBody RegisterStaffRequest request) {
        RegisterResponse response = registrationService.registerStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
