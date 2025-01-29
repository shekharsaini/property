package com.sc.property.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignProperty {
  private Long propertyId;
  private String propertyName;
  private Long tenantId;
  private Long agentId;
}
