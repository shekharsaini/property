package com.sc.property.management.dto;

import com.sc.property.management.datasource.Document;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentDto {
  private final Long id;
  private final String fileName;
  private final String filePath;
  private final String contentType;
  private final String uploadedBy;
  private final Long propertyId;
  private final LocalDateTime uploadedAt;

  public DocumentDto(Document document) {
    this.id = document.getId();
    this.fileName = document.getFileName();
    this.filePath = document.getFilePath();
    this.contentType = document.getContentType();
    this.uploadedBy = document.getUploadedBy().getUsername();
    this.propertyId = document.getProperty().getId();
    this.uploadedAt = document.getUploadedAt();
  }
}
