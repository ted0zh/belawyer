package com.lawyer.belawyer.repository;

import com.lawyer.belawyer.data.dto.DocumentSummaryDto;
import com.lawyer.belawyer.data.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    Optional<Document> findById(Long id);
    @Query("""
      SELECT new com.lawyer.belawyer.data.dto.DocumentSummaryDto(
        d.id, d.name, d.type, d.summary
      )
      FROM Document d
      WHERE d.caseEntity.id = :caseId
    """)
    List<DocumentSummaryDto> findSummariesByCaseEntityId(@Param("caseId") Long caseId);

    List<Document> findByCaseEntityId(Long caseId);
    @Modifying
    @Query("UPDATE Document d SET d.summary = NULL WHERE d.id = :id")
    void clearSummary(@Param("id") Long id);

}
