package com.sc.property.management.util;

public enum UserSortField {
  ID("id"),
  USERNAME("username"),
  EMAIL("email"),
  PHONE("phone");

  private final String field;

  UserSortField(String field) {
    this.field = field;
  }

  public String getField() {
    return field;
  }
}
