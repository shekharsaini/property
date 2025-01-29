package com.sc.property.management.util;

public enum UserType {
  TENANT("TENANT"),
  LANDLORD("LANDLORD"),
  ADMIN("ADMIN"),
  AGENT("AGENT");

  private final String userType;

  UserType(String userType) {
    this.userType = userType;
  }

  public String getMessage() {
    return userType;
  }
}
