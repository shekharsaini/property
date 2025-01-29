package com.sc.property.management.service;

import com.sc.property.management.datasource.Property;
import com.sc.property.management.dto.AssignProperty;
import com.sc.property.management.dto.PropertyDto;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyService {
  PropertyDto addProperty(PropertyDto property);

  Page<PropertyDto> getProperty(
      Long propertyId,
      String name,
      String status,
      String availability,
      Long agentId,
      Long tenantId,
      Long landlordId,
      Pageable pageable);

  Property deleteProperty(Long propertyId);

  Page<PropertyDto> getPropertiesByUserName(String userName, Pageable pageable);

  Map<String, Long> getPropertyCounts(Long landlordId, Long agentId, Long tenantId);

  void assignPropertyToUser(AssignProperty assignProperty);
}
