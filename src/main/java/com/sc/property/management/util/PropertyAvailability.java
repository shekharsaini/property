package com.sc.property.management.util;

public enum PropertyAvailability {
  OPEN("OPEN"),
  OCCUPIED("OCCUPIED"),
  IN_NOTICE("IN NOTICE"),
  UNDER_MAINTENANCE("UNDER MAINTENANCE");

  private final String availability;

  PropertyAvailability(String availability) {
    this.availability = availability;
  }

  public String getMessage() {
    return availability;
  }
}
