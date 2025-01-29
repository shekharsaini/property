package com.sc.property.management.service;

import com.sc.property.management.datasource.Insurance;
import com.sc.property.management.datasource.Property;
import com.sc.property.management.datasource.User;
import com.sc.property.management.dto.InsuranceDto;
import com.sc.property.management.repository.InsuranceRepository;
import com.sc.property.management.repository.PropertyRepository;
import com.sc.property.management.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class InsuranceService {

  @Autowired private InsuranceRepository insuranceRepository;

  @Autowired private PropertyRepository propertyRepository;

  @Autowired private UserRepository userRepository;

  public Insurance uploadInsurance(
      Long propertyId,
      Long userId,
      String providerName,
      LocalDate startDate,
      LocalDate endDate,
      byte[] document,
      String docName) {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property not found"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Insurance insurance = new Insurance();
    insurance.setInsuranceProviderName(providerName);
    insurance.setStartDate(startDate);
    insurance.setEndDate(endDate);
    insurance.setInsuranceDocument(document);
    insurance.setProperty(property);
    insurance.setUpdatedBy(user);
    insurance.setInsuranceDocumentName(docName);

    return insuranceRepository.save(insurance);
  }

  public List<InsuranceDto> fetchInsurance(Long propertyId) {

    List<InsuranceDto> insurances =
        insuranceRepository.findByPropertyId(propertyId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(insurances)) {
      return Collections.emptyList();
    }
    return insurances;
  }

  public void deleteInsurance(Long id, Long propertyId) {
    Insurance insurance =
        insuranceRepository
            .findByIdAndPropertyId(id, propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Insurance not found for property"));
    insuranceRepository.delete(insurance);
  }

  private InsuranceDto convertToDto(Insurance insurance) {
    return new InsuranceDto(
        insurance.getId(),
        insurance.getInsuranceProviderName(),
        insurance.getStartDate(),
        insurance.getEndDate(),
        insurance.getProperty().getId(),
        insurance.getUpdatedBy().getId());
  }

  public ByteArrayOutputStream getAllDocumentsAsZip(Long propertyId) throws IOException {
    List<Insurance> documents =
        insuranceRepository.findByPropertyId(propertyId); // Get all documents from DB
    if (documents.isEmpty()) {
      return null;
    }

    // Create a zip output stream in memory
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {

      for (Insurance insurance : documents) {
        byte[] content = insurance.getInsuranceDocument(); // Assuming content is stored as byte[]

        if (content != null && content.length > 0) {
          // Create a new zip entry for each document
          ZipEntry zipEntry = new ZipEntry(insurance.getInsuranceDocumentName());
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
