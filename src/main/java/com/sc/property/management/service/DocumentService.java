package com.sc.property.management.service;

import com.sc.property.management.datasource.Document;
import com.sc.property.management.datasource.Property;
import com.sc.property.management.datasource.User;
import com.sc.property.management.dto.DocumentDto;
import com.sc.property.management.exception.RecordExistException;
import com.sc.property.management.repository.DocumentRepository;
import com.sc.property.management.repository.PropertyRepository;
import com.sc.property.management.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

  @Value("${document.upload-dir:src/main/resources/static/uploads}")
  private String uploadDir;

  @Autowired private DocumentRepository documentRepository;

  @Autowired private PropertyRepository propertyRepository;

  @Autowired private UserRepository userRepository;

  @Transactional
  public List<DocumentDto> uploadDocument(Long propertyId, Long userId, List<MultipartFile> files)
      throws IOException {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

    List<Document> uploadedDocuments = new ArrayList<>();
    for (MultipartFile file : files) {
      // Save the file
      String fileName = file.getOriginalFilename();

      // Create Document record
      Document document = new Document();
      Document docExist = documentRepository.findByFileName(fileName);
      if (Objects.nonNull(docExist)) {
        throw new RecordExistException("Duplicate file added, please add unique file");
      }
      document.setFileName(fileName);
      document.setFilePath("TEST");
      document.setContentType(file.getContentType());
      document.setUploadedBy(user);
      document.setProperty(property);
      document.setUploadedAt(LocalDateTime.now());
      byte[] content = file.getBytes();
      document.setContent(content);
      documentRepository.save(document);
      uploadedDocuments.add(document);
    }

    return uploadedDocuments.stream().map(DocumentDto::new).collect(Collectors.toList());
  }

  public Document downloadDocument(Long documentId) throws IOException {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Document not found"));
    return document;
  }

  public List<DocumentDto> getDocumentsByProperty(Long propertyId) {
    List<Document> documents = documentRepository.findByPropertyId(propertyId);
    if (CollectionUtils.isEmpty(documents)) {
      throw new EntityNotFoundException("Documents not found with property id : " + propertyId);
    }
    return documents.stream().map(DocumentDto::new).collect(Collectors.toList());
  }

  public void deleteDocument(Long documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Document not found"));

    // Delete the physical file from the resources folder
    try {
      Files.deleteIfExists(Paths.get(document.getFilePath()));
    } catch (IOException e) {
      throw new RuntimeException("Could not delete file: " + document.getFilePath(), e);
    }

    documentRepository.delete(document);
  }

  public ByteArrayOutputStream getAllDocumentsAsZip(Long propertyId) throws IOException {
    List<Document> documents =
        documentRepository.findByPropertyId(propertyId); // Get all documents from DB
    if (documents.isEmpty()) {
      return null;
    }

    // Create a zip output stream in memory
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {

      for (Document document : documents) {
        byte[] content = document.getContent(); // Assuming content is stored as byte[]

        if (content != null && content.length > 0) {
          // Create a new zip entry for each document
          ZipEntry zipEntry = new ZipEntry(document.getFileName());
          zos.putNextEntry(zipEntry);

          // Write the file content into the zip entry
          zos.write(content);
          zos.closeEntry();
        }
      }

      // Finalize the zip output stream
      zos.finish();
    }

    // Return the byte array output stream with zipped content
    return byteArrayOutputStream;
  }
}
