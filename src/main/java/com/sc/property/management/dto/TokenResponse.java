package com.sc.property.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
  @JsonProperty("access_token")
  private String access_token;

  private String firm;

  // Other fields if necessary
}
