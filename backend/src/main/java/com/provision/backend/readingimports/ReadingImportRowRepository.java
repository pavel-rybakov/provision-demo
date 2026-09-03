package com.provision.backend.readingimports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.stream.Stream;

public interface ReadingImportRowRepository extends JpaRepository<ReadingImportRow, UUID> {
    @Query("select row from ReadingImportRow row where row.readingImport.id = :importId order by row.rowNumber")
    Stream<ReadingImportRow> streamAllByImportId(@Param("importId") UUID importId);
}
