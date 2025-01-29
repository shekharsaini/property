package com.sc.property.management.dto;

import lombok.Data;

@Data
public class AddressValidationResponse {
  private boolean valid;
  private String displayName; // Geocoded display name for the address
  private String message;

  public AddressValidationResponse(boolean b, String display_name, String s) {}

  // Constructor, Getters, Setters
}
