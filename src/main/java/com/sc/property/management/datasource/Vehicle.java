package com.sc.property.management.datasource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Data;

@Entity
@Table(name = "vehicle_info")
@Data
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String make;
  private String model;
  private String color;

  @Column(name = "licence_number")
  private String licenseNumber;

  @Column(name = "model_year")
  private LocalDate modelYear;
}
