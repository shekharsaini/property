package com.sc.property.management.util;

public enum Status {
  SUCCESS("Success"),
  FAIL("Fail");

  private final String status;

  Status(String status) {
    this.status = status;
  }

  public String getMessage() {
    return status;
  }
}
