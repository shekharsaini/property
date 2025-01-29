package com.sc.property.management.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class InsuranceDto {
  private Long id;
  private String insuranceProviderName;
  private LocalDate startDate;
  private LocalDate endDate;
  private Long propertyId;
  private Long updatedBy;

  // Constructors, Getters, and Setters
  public InsuranceDto() {}

  public InsuranceDto(
      Long id,
      String insuranceProviderName,
      LocalDate startDate,
      LocalDate endDate,
      Long propertyId,
      Long updatedBy) {
    this.id = id;
    this.insuranceProviderName = insuranceProviderName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.propertyId = propertyId;
    this.updatedBy = updatedBy;
  }

  // Getters and Setters
  // ...
}
