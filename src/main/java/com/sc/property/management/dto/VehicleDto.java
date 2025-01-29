package com.sc.property.management.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto {
  private String make;
  private String model;
  private LocalDate modelYear;
  private String licenseNumber;
  private String color;
}
