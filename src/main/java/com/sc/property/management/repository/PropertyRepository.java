package com.sc.property.management.repository;

import com.sc.property.management.datasource.Property;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository
    extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

  @Query("select p from Property p where p.createdBy.username=:username and p.status='ACTIVE'")
  Page<Property> findByCreatedBy(@Param("username") String username, Pageable pageable);

  long countByStatus(String status);

  Optional<Property> findByPropertyName(String propertyName);

  @Query("SELECT COUNT(p) FROM Property p WHERE p.createdBy.id = :landlordId")
  long countByCreatedById(@Param("landlordId") Long landlordId);

  @Query("SELECT COUNT(p) FROM Property p WHERE p.agentId = :agentId")
  long countByAgentId(@Param("agentId") Long agentId);

  @Query("SELECT COUNT(p) FROM Property p WHERE p.tenantId = :tenantId")
  long countByTenantId(@Param("tenantId") Long tenantId);
}
