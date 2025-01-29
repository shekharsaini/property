package com.sc.property.management.datasource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.LocalDate;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "insurance")
@Data
public class Insurance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "insurance_provider_name", nullable = false)
  private String insuranceProviderName;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Lob
  @JdbcTypeCode(Types.BINARY)
  @Column(name = "insurance_document", nullable = false)
  private byte[] insuranceDocument;

  @Column(name = "insurance_document_name", nullable = false)
  private String insuranceDocumentName;

  @ManyToOne
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  @ManyToOne
  @JoinColumn(name = "updated_by", nullable = false)
  private User updatedBy;

  // Getters and Setters
}
