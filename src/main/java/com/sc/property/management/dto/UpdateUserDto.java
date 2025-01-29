package com.sc.property.management.dto;

import com.sc.property.management.util.UserType;
import lombok.Data;

@Data
public class UpdateUserDto {
  private String userId;
  private String userName;
  private String email;
  private String emergencyContactName;
  private Long emergencyContactNumber;
  private String emergencyContactEmail;
  private AddressValidationRequest emergencyContactAddress;
  private UserType userType;
  private VehicleDto vehicleDto;
}
