package com.sc.property.management.service;

import com.sc.property.management.datasource.Address;
import com.sc.property.management.datasource.Property;
import com.sc.property.management.datasource.User;
import com.sc.property.management.dto.AddressValidationRequest;
import com.sc.property.management.dto.AssignProperty;
import com.sc.property.management.dto.PropertyDto;
import com.sc.property.management.exception.CustomValidationException;
import com.sc.property.management.exception.UserNotFoundException;
import com.sc.property.management.repository.PropertyRepository;
import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.util.PropertyAvailability;
import com.sc.property.management.util.PropertyConstants;
import com.sc.property.management.util.PropertySpecification;
import com.sc.property.management.util.PropertyStatus;
import com.sc.property.management.util.UserType;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PropertyServiceImpl implements PropertyService {

  @Autowired private PropertyRepository propertyRepository;

  @Autowired private AddressValidationService addressValidationService;

  @Autowired private UserRepository userRepository;

  @Override
  public PropertyDto addProperty(PropertyDto propertyDto) {
    AddressValidationRequest addressValidationRequest = propertyDto.getAddress();
    PropertyAvailability availability;
    boolean isAddressValid = addressValidationService.validateAddress(addressValidationRequest);

    if (!isAddressValid) {
      throw new CustomValidationException("Invalid address, please provide a valid address.");
    }
    Optional<User> optionalUser = userRepository.findByUsername(propertyDto.getCreatedBy());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User not found : " + propertyDto.getCreatedBy());
    }
    try {
      availability =
          PropertyAvailability.valueOf(propertyDto.getPropertyAvailability().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CustomValidationException(
          "Invalid property availability value: " + propertyDto.getPropertyAvailability());
    }
    Optional<Property> optionalProperty =
        propertyRepository.findByPropertyName(propertyDto.getPropertyName());
    if (optionalProperty.isPresent()) {
      throw new CustomValidationException(PropertyConstants.DUPLICATE_PROPERTY_NAME);
    }
    User user = optionalUser.get();
    Address address =
        Address.builder()
            .city(propertyDto.getAddress().getCity())
            .country(propertyDto.getAddress().getCountry())
            .postalCode(propertyDto.getAddress().getZipcode())
            .state(propertyDto.getAddress().getState())
            .street(propertyDto.getAddress().getStreet())
            .build();
    Property property =
        Property.builder()
            .address(address)
            .type(propertyDto.getType())
            .size(propertyDto.getSize())
            .price(propertyDto.getPrice())
            .amenities(propertyDto.getAmenities())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(user)
            .updatedBy(user)
            .status(PropertyStatus.ACTIVE.getMessage())
            .propertyName(propertyDto.getPropertyName())
            .propertyAvailability(availability)
            .build();
    return mapToPropertyDto(propertyRepository.save(property));
  }

  PropertyDto mapToPropertyDto(Property property) {
    PropertyDto propertyDto = new PropertyDto();
    propertyDto.setPropertyName(property.getPropertyName());
    propertyDto.setAddress(mapToAddressDto(property.getAddress()));
    propertyDto.setAmenities(property.getAmenities());
    propertyDto.setPrice(property.getPrice());
    propertyDto.setCreatedBy(property.getCreatedBy().getUsername());
    propertyDto.setStatus(property.getStatus());
    propertyDto.setId(property.getId());
    propertyDto.setSize(property.getSize());
    propertyDto.setType(property.getType());
    propertyDto.setUpdatedBy(property.getUpdatedBy().getUsername());
    return propertyDto;
  }

  AddressValidationRequest mapToAddressDto(Address address) {
    return AddressValidationRequest.builder()
        .zipcode(address.getPostalCode())
        .street(address.getStreet())
        .city(address.getCity())
        .state(address.getState())
        .country(address.getCountry())
        .build();
  }

  @Override
  public Page<PropertyDto> getProperty(
      Long propertyId,
      String name,
      String status,
      String availability,
      Long agentId,
      Long tenantId,
      Long landlordId,
      Pageable pageable) {

    // Validate availability only if it's provided
    if (availability != null
        && !availability.isEmpty()
        && Arrays.stream(PropertyAvailability.values())
            .noneMatch(value -> value.name().equalsIgnoreCase(availability))) {
      throw new CustomValidationException(
          "Invalid property availability value. Valid values: OPEN, OCCUPIED, IN NOTICE, UNDER MAINTENANCE.");
    }

    // Validate status only if it's provided
    if (status != null
        && !status.isEmpty()
        && !status.equalsIgnoreCase(PropertyStatus.ACTIVE.getMessage())
        && !status.equalsIgnoreCase(PropertyStatus.INACTIVE.getMessage())) {
      throw new CustomValidationException("Invalid status value. Valid values: ACTIVE, INACTIVE.");
    }

    // Retrieve agent and tenant details if IDs are provided
    User agent = null;
    User tenant = null;
    User landlord = null;

    if (agentId != null) {
      agent =
          userRepository
              .findById(agentId)
              .orElseThrow(
                  () -> new EntityNotFoundException("Agent with id " + agentId + " not found"));
    }

    if (tenantId != null) {
      tenant =
          userRepository
              .findById(tenantId)
              .orElseThrow(
                  () -> new EntityNotFoundException("Tenant with id " + tenantId + " not found"));
    }

    if (landlordId != null) {
      landlord =
          userRepository
              .findById(landlordId)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException("Landlord with id " + landlordId + " not found"));
    }

    Specification<Property> spec =
        PropertySpecification.filterProperties(
            propertyId, name, status, availability, agent, tenant, landlord);
    Page<Property> properties = propertyRepository.findAll(spec, pageable);

    // Handle not found cases for combinations of filters
    if (properties.isEmpty()) {
      StringBuilder errorMessage = new StringBuilder("Property details not found");

      if (propertyId != null) {
        errorMessage.append(" for propertyId: ").append(propertyId);
      }

      if (name != null && !name.isEmpty()) {
        errorMessage.append(" and name: ").append(name);
      }

      if (status != null && !status.isEmpty()) {
        errorMessage.append(" and status: ").append(status);
      }

      if (availability != null && !availability.isEmpty()) {
        errorMessage.append(" and availability: ").append(availability);
      }

      if (agentId != null) {
        errorMessage.append(" and agentId: ").append(agentId);
      }

      if (tenantId != null) {
        errorMessage.append(" and tenantId: ").append(tenantId);
      }

      if (landlordId != null) {
        errorMessage.append(" and landlordId: ").append(landlordId);
      }

      throw new EntityNotFoundException(errorMessage.toString());
    }

    return properties.map(this::convertToDto);
  }

  // Convert Property to PropertyDto
  private PropertyDto convertToDto(Property property) {
    return new PropertyDto(
        property.getId(),
        AddressValidationRequest.builder()
            .city(property.getAddress().getCity())
            .country(property.getAddress().getCountry())
            .state(property.getAddress().getState())
            .street(property.getAddress().getStreet())
            .zipcode(property.getAddress().getPostalCode())
            .build(),
        property.getType(),
        property.getSize(),
        property.getPrice(),
        property.getAmenities(),
        property.getCreatedBy().getUsername(),
        property.getUpdatedBy().getUsername(),
        property.getStatus(),
        property.getPropertyName(),
        property.getPropertyAvailability().name(),
        property.getTenantId(),
        property.getAgentId());
  }

  @Override
  public Property deleteProperty(Long propertyId) {
    Optional<Property> optionalProperty = propertyRepository.findById(propertyId);
    if (optionalProperty.isEmpty()) {
      throw new EntityNotFoundException("Property details not found by id : " + propertyId);
    }
    Property property = optionalProperty.get();
    property = property.toBuilder().status(PropertyStatus.INACTIVE.getMessage()).build();
    return propertyRepository.save(property);
  }

  @Override
  public Page<PropertyDto> getPropertiesByUserName(String userName, Pageable pageable) {
    User user =
        userRepository
            .findByUsername(userName)
            .orElseThrow(() -> new UserNotFoundException(PropertyConstants.ERROR_USER_NOT_FOUND));
    Page<Property> properties = propertyRepository.findByCreatedBy(user.getUsername(), pageable);
    return properties.map(this::convertToDto);
  }

  @Override
  public Map<String, Long> getPropertyCounts(Long landlordId, Long agentId, Long tenantId) {
    long totalProperties = propertyRepository.count();

    // Count active and inactive properties
    long activeProperties = propertyRepository.countByStatus("ACTIVE");
    long inactiveProperties = propertyRepository.countByStatus("INACTIVE");

    // Count properties based on landlordId, agentId, and tenantId
    long propertiesByLandlord =
        landlordId != null ? propertyRepository.countByCreatedById(landlordId) : 0;
    long propertiesByAgent = agentId != null ? propertyRepository.countByAgentId(agentId) : 0;
    long propertiesByTenant = tenantId != null ? propertyRepository.countByTenantId(tenantId) : 0;

    // Prepare response map
    Map<String, Long> counts = new HashMap<>();
    counts.put("totalProperties", totalProperties);
    counts.put("activeProperties", activeProperties);
    counts.put("inactiveProperties", inactiveProperties);
    counts.put("propertiesByLandlord", propertiesByLandlord);
    counts.put("propertiesByAgent", propertiesByAgent);
    counts.put("propertiesByTenant", propertiesByTenant);

    return counts;
  }

  @Override
  public void assignPropertyToUser(AssignProperty assignProperty) {
    // Validate the property
    Property property =
        propertyRepository
            .findById(assignProperty.getPropertyId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Property not found with ID: " + assignProperty.getPropertyId()));
    if (PropertyStatus.INACTIVE.name().equalsIgnoreCase(property.getStatus())) {
      throw new CustomValidationException("Property is inactive");
    }

    if (Objects.isNull(assignProperty.getAgentId())
        && Objects.isNull(assignProperty.getTenantId())) {
      throw new CustomValidationException("Either agent id or tenant id must be provided");
    }

    // Validate the user
    User user =
        userRepository
            .findById(
                Objects.nonNull(assignProperty.getAgentId())
                    ? assignProperty.getAgentId()
                    : assignProperty.getTenantId())
            .orElseThrow(
                () -> new EntityNotFoundException("User not found with provided agent or tenant"));

    if (!UserType.TENANT.name().equals(user.getUserType())
        && !UserType.AGENT.name().equals(user.getUserType())) {
      throw new CustomValidationException("Property can be assigned to only Agent or Tenant");
    }

    // Assign the property to the user
    if (Objects.nonNull(assignProperty.getTenantId())) {
      property = property.toBuilder().tenantId(assignProperty.getTenantId()).build();
    }
    if (Objects.nonNull(assignProperty.getAgentId())) {
      property = property.toBuilder().agentId(assignProperty.getAgentId()).build();
    }
    propertyRepository.save(property);
  }
}
