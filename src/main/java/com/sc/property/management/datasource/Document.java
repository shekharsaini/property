package com.sc.property.management.datasource;

import jakarta.persistence.*;
import java.sql.Types;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "document")
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Lob
  @JdbcTypeCode(Types.BINARY)
  @Column(name = "file_content", nullable = false)
  private byte[] content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by", referencedColumnName = "id", nullable = false)
  private User uploadedBy; // Assume you have a User entity

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", referencedColumnName = "id", nullable = false)
  private Property property; // Assume you have a Property entity

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public User getUploadedBy() {
    return uploadedBy;
  }

  public void setUploadedBy(User uploadedBy) {
    this.uploadedBy = uploadedBy;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property property) {
    this.property = property;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }
}
