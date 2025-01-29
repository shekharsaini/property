package com.sc.property.management.repository;

import com.sc.property.management.datasource.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
  List<Document> findByPropertyId(Long propertyId);

  Document findByFileName(String fileName);
}
