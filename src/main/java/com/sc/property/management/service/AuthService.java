package com.sc.property.management.service;

import com.sc.property.management.dto.AuthRequest;
import com.sc.property.management.dto.AuthResponse;

public interface AuthService {
  AuthResponse validateUser(AuthRequest authRequest);
}
