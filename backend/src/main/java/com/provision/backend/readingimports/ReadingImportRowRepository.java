package com.provision.backend.readingimports;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReadingImportRowRepository extends JpaRepository<ReadingImportRow, UUID> {
    List<ReadingImportRow> findAllByReadingImportIdOrderByRowNumber(UUID importId);
}
