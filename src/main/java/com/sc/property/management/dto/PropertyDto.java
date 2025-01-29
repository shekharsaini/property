package com.sc.property.management.dto;

import com.sc.property.management.util.PropertyAvailability;
import com.sc.property.management.validation.ValueOfEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDto {

  private Long id;
  @Valid private AddressValidationRequest address;
  private String type;
  private double size;
  private double price;
  private String amenities;

  @NotBlank(message = "Please provide the name of the user who is creating the property")
  private String createdBy;

  @NotBlank(message = "Please provide the name of the user who is updating the property")
  private String updatedBy;

  private String status;

  @NotBlank(message = "Please provide property name")
  private String propertyName;

  @NotBlank(message = "Property availability is required")
  @ValueOfEnum(
      enumClass = PropertyAvailability.class,
      message =
          "Invalid property availability. Valid values: OPEN, OCCUPIED, IN NOTICE, UNDER MAINTENANCE")
  private String propertyAvailability = "OPEN";

  private Long tenantId;
  private Long agentId;
  // Getters and Setters
}
