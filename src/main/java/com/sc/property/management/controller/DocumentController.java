package com.sc.property.management.controller;

import com.sc.property.management.datasource.Document;
import com.sc.property.management.dto.ApiResponse;
import com.sc.property.management.dto.DocumentDto;
import com.sc.property.management.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

  @Autowired private DocumentService documentService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload a document",
      description = "Upload a file with userId and propertyId")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File uploaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input")
      })
  public ResponseEntity<?> uploadDocument(
      @RequestParam("propertyId") Long propertyId,
      @RequestParam("userId") Long userId,
      @RequestParam("files") List<MultipartFile> files)
      throws IOException {
    List<DocumentDto> documents = documentService.uploadDocument(propertyId, userId, files);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                HttpStatus.CREATED.value(), "Success", "Document uploaded", documents));
  }

  @GetMapping("/download/{documentId}")
  public ResponseEntity<?> downloadDocument(@PathVariable Long documentId) {
    try {
      Document document = documentService.downloadDocument(documentId);
      HttpHeaders headers = new HttpHeaders();
      headers.set(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"" + document.getFileName() + "\"");
      headers.setContentType(MediaType.parseMediaType(document.getContentType()));
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType("application/octet-stream"))
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + document.getFileName() + "\"")
          .body(document.getContent());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/property/{propertyId}")
  public ResponseEntity<?> getDocumentsByProperty(@PathVariable Long propertyId) {
    List<DocumentDto> documents = documentService.getDocumentsByProperty(propertyId);
    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(), "Success", "Documents fetched", documents));
  }

  @DeleteMapping("/{documentId}")
  public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
    documentService.deleteDocument(documentId);
    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(), "Success", "Document deleted", null));
  }

  @GetMapping("/download-all-zip/{propertyId}")
  public ResponseEntity<InputStreamResource> downloadAllDocumentsAsZip(
      @PathVariable Long propertyId) {
    try {
      // Call the service to get the zipped file in memory
      ByteArrayOutputStream zipOutputStream = documentService.getAllDocumentsAsZip(propertyId);
      if (zipOutputStream == null) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      }

      // Prepare the response to download the zip file
      InputStreamResource resource =
          new InputStreamResource(new ByteArrayInputStream(zipOutputStream.toByteArray()));
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documents.zip");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(zipOutputStream.size())
          .contentType(MediaType.parseMediaType("application/zip"))
          .body(resource);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
