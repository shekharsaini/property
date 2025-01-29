package com.sc.property.management.util;

public enum PropertyStatus {
  ACTIVE("ACTIVE"),
  INACTIVE("INACTIVE");

  private final String status;

  PropertyStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return status;
  }
}
