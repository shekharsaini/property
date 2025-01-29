package com.sc.property.management.dto;

import lombok.Data;

@Data
public class AuthResponse {
  private String jwt;
  private String userId;
  private String userType;

  public AuthResponse(String jwt, String userId, String userType) {
    this.jwt = jwt;
    this.userId = userId;
    this.userType = userType;
  }
}
