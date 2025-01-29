package com.sc.property.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteRequest {
  @NotBlank(message = "Email is mandatory.")
  @Email(message = "Invalid email format.")
  private String email;

  @NotBlank(message = "Username is mandatory.")
  private String username;

  @NotBlank(message = "Phone number is mandatory.")
  @Pattern(regexp = "^[0-9]{10}$", message = "Phone number should be 10 digits")
  private String phoneNo;

  @NotBlank(message = "User type is mandatory.")
  private String userType;
}
