package com.sc.property.management.controller;

import com.sc.property.management.datasource.Insurance;
import com.sc.property.management.dto.ApiResponse;
import com.sc.property.management.dto.InsuranceDto;
import com.sc.property.management.service.InsuranceService;
import com.sc.property.management.util.PropertyConstants;
import com.sc.property.management.util.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

  @Autowired private InsuranceService insuranceService;

  @PostMapping(value = "/uploadInsurance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
  public ResponseEntity<?> uploadInsurance(
      @RequestParam Long propertyId,
      @RequestParam Long userId,
      @RequestParam String providerName,
      @RequestParam(defaultValue = "1900-01-31")
          @Parameter(
              description = "Start date of the insurance",
              schema = @Schema(defaultValue = "2024-01-01"))
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(defaultValue = "1900-01-31")
          @Parameter(
              description = "End date of the insurance",
              schema = @Schema(defaultValue = "2024-01-01"))
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(value = "document") MultipartFile document) {

    try {
      String docName = document.getOriginalFilename();
      byte[] docBytes = document.getBytes();
      Insurance insurance =
          insuranceService.uploadInsurance(
              propertyId, userId, providerName, startDate, endDate, docBytes, docName);
      return ResponseEntity.ok(
          new ApiResponse<>(
              HttpStatus.OK.value(),
              Status.SUCCESS.getMessage(),
              PropertyConstants.SUCCESS_INSURANCE_ADDED,
              null));
    } catch (IOException e) {
      throw new RuntimeException("Error processing file");
    }
  }

  //  @GetMapping("/fetchInsurance/{propertyId}")
  public ResponseEntity<?> fetchInsurance(@PathVariable Long propertyId) {
    List<InsuranceDto> insurance = insuranceService.fetchInsurance(propertyId);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_INSURANCE_FETCHED,
            insurance));
  }

  //  @DeleteMapping("/deleteInsurance/{id}")
  public ResponseEntity<?> deleteInsurance(@PathVariable Long id, @RequestParam Long propertyId) {
    insuranceService.deleteInsurance(id, propertyId);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_INSURANCE_DELETED,
            null));
  }

  //  @GetMapping("/download-all-zip/{propertyId}")
  public ResponseEntity<InputStreamResource> downloadAllDocumentsAsZip(
      @PathVariable Long propertyId) {
    try {
      // Call the service to get the zipped file in memory
      ByteArrayOutputStream zipOutputStream = insuranceService.getAllDocumentsAsZip(propertyId);
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
