package com.sc.property.management.repository;

import com.sc.property.management.datasource.Insurance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
  Optional<Insurance> findByIdAndPropertyId(Long id, Long propertyId);

  List<Insurance> findByPropertyId(Long propertyId);
}
