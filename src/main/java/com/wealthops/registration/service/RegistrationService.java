package com.wealthops.registration.service;

import com.wealthops.registration.dto.RegisterClientRequest;
import com.wealthops.registration.dto.RegisterResponse;
import com.wealthops.registration.dto.RegisterStaffRequest;

public interface RegistrationService {

    RegisterResponse registerClient(RegisterClientRequest request);

    RegisterResponse registerStaff(RegisterStaffRequest request);
}