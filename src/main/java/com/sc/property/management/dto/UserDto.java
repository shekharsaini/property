package com.sc.property.management.dto;

import com.sc.property.management.util.PropertyConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {

  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  private String email;

  private String username;

  @Pattern(regexp = "^[0-9]{10}$", message = "Phone number should be 10 digits")
  @NotBlank(message = "Phone number is required")
  private String phone;

  @Pattern(
      regexp = PropertyConstants.PASSWORD_REGEX,
      message = PropertyConstants.ERROR_WEAK_PASSWORD)
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password should be at least 8 characters long")
  private String password;

  @NotBlank(message = "User type is mandatory. Ex TENANT, LANDLORD, ADMIN, AGENT")
  private String userType;
}
