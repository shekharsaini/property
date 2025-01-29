package com.sc.property.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AddressValidationRequest {
  private String secondaryAddress;

  @NotBlank(message = "Please provide street information")
  private String street;

  @NotBlank(message = "Please provide city information")
  private String city;

  @NotBlank(message = "Please provide state information")
  private String state;

  private String zipcode;

  @NotBlank(message = "Please provide country information")
  private String country;
  // Getters and Setters
}
