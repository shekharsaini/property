package com.sc.property.management.util;

public enum OtpType {
  EMAIL("email"),
  PHONE("phone");

  private final String type;

  OtpType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return type;
  }
}
