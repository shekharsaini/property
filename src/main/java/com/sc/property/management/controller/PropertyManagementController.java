package com.sc.property.management.controller;

import com.sc.property.management.dto.ApiResponse;
import com.sc.property.management.dto.AssignProperty;
import com.sc.property.management.dto.PropertyDto;
import com.sc.property.management.service.PropertyService;
import com.sc.property.management.util.PropertyConstants;
import com.sc.property.management.util.Status;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/property")
@Slf4j
public class PropertyManagementController {
  @Autowired private PropertyService propertyService;

  @PostMapping("/add")
  public ResponseEntity<?> addProperty(@Valid @RequestBody PropertyDto property) {
    PropertyDto propertyDto = propertyService.addProperty(property);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.CREATED.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_PROPERTY_REGISTERED,
            propertyDto));
  }

  @GetMapping("/get")
  public ResponseEntity<?> getProperties(
      @RequestParam(name = "propertyId", required = false) Long propertyId,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "availability", required = false) String availability,
      @RequestParam(name = "agentId", required = false) Long agentId,
      @RequestParam(name = "tenantId", required = false) Long tenantId,
      @RequestParam(name = "landlordId", required = false) Long landlordId,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<PropertyDto> propertyPage =
        propertyService.getProperty(
            propertyId, name, status, availability, agentId, tenantId, landlordId, pageable);

    if (propertyPage.hasContent()) {
      return ResponseEntity.ok(
          new ApiResponse<>(
              HttpStatus.OK.value(),
              Status.SUCCESS.getMessage(),
              PropertyConstants.SUCCESS_PROPERTY_FETCHED,
              propertyPage));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ApiResponse<>(
                  HttpStatus.NOT_FOUND.value(),
                  Status.FAIL.getMessage(),
                  PropertyConstants.ERROR_PROPERTY_NOT_FOUND,
                  null));
    }
  }

  @DeleteMapping("/delete/{propertyId}")
  public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
    propertyService.deleteProperty(propertyId);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_PROPERTY_DELETED,
            null));
  }

  @GetMapping("/user/{userName}")
  public ResponseEntity<?> getPropertiesByUserId(
      @PathVariable String userName,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_PROPERTY_FETCHED,
            propertyService.getPropertiesByUserName(userName, pageable)));
  }

  @GetMapping("/counts")
  public ResponseEntity<?> getPropertyCounts(
      @RequestParam(value = "landlordId", required = false) Long landlordId,
      @RequestParam(value = "agentId", required = false) Long agentId,
      @RequestParam(value = "tenantId", required = false) Long tenantId) {

    Map<String, Long> propertyCounts =
        propertyService.getPropertyCounts(landlordId, agentId, tenantId);

    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.PROPERTY_COUNTS_FETCHED,
            propertyCounts));
  }

  @PostMapping("/assign")
  @PreAuthorize("hasRole('ROLE_LANDLORD') || hasRole('ROLE_ADMIN')")
  public ResponseEntity<String> assignProperty(@RequestBody AssignProperty assignProperty) {
    propertyService.assignPropertyToUser(assignProperty);
    return ResponseEntity.ok("Property assigned successfully.");
  }
}
