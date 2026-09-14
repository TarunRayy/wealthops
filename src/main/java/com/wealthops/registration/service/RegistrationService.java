package com.wealthops.registration.service;

import com.wealthops.registration.dto.RegisterClientRequest;
import com.wealthops.registration.dto.RegisterResponse;
import com.wealthops.registration.dto.RegisterStaffRequest;

import java.util.List;

public interface RegistrationService {

    RegisterResponse registerClient(RegisterClientRequest request);

    RegisterResponse registerStaff(RegisterStaffRequest request, String requesterEmail);
    List<RegisterResponse> getAllStaff(String requesterEmail);
    List<RegisterResponse> getStaffByBranch(Long branchId, String requesterEmail);
    List<RegisterResponse> getRelationshipManagers(String requesterEmail);
}