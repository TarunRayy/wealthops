package com.wealthops.auth.service;

import com.wealthops.auth.dto.LoginRequest;
import com.wealthops.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
