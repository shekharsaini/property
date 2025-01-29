package com.sc.property.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordDto {
  @NotBlank private String usernameOrEmail;
}
