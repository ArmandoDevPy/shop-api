package com.armando.shop_api.service;

import com.armando.shop_api.dto.*;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
