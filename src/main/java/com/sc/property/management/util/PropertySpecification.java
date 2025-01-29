package com.sc.property.management.util;

import com.sc.property.management.datasource.Property;
import com.sc.property.management.datasource.User;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PropertySpecification {

  public static Specification<Property> filterProperties(
      Long propertyId,
      String name,
      String status,
      String availability,
      User agent,
      User tenant,
      User landlord) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (propertyId != null) {
        predicates.add(criteriaBuilder.equal(root.get("id"), propertyId));
      }

      if (name != null && !name.isEmpty()) {
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("propertyName")), "%" + name.toLowerCase() + "%"));
      }

      if (status != null && !status.isEmpty()) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }

      if (availability != null && !availability.isEmpty()) {
        predicates.add(criteriaBuilder.equal(root.get("propertyAvailability"), availability));
      }

      if (agent != null) {
        predicates.add(criteriaBuilder.equal(root.get("agentId"), agent.getId()));
      }

      if (tenant != null) {
        predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenant.getId()));
      }

      if (landlord != null) {
        predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), landlord.getId()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
