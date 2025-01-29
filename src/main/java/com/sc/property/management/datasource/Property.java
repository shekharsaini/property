package com.sc.property.management.datasource;

import com.sc.property.management.util.PropertyAvailability;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@Entity
@Table(name = "property")
public class Property {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "address_id", referencedColumnName = "id")
  private Address address;

  private String type; // E.g., apartment, house, commercial
  private double size; // Size in square feet or meters
  private double price; // Price of the property
  private String status;

  @Column(name = "property_name")
  private String propertyName;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Column(name = "agent_id")
  private Long agentId;

  @Column(length = 1000) // To allow more characters for amenities
  private String amenities; // List of amenities as a comma-separated string

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
  private User createdBy;

  @ManyToOne
  @JoinColumn(name = "updated_by", referencedColumnName = "id", nullable = false)
  private User updatedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_availability", nullable = false)
  private PropertyAvailability propertyAvailability;
}
