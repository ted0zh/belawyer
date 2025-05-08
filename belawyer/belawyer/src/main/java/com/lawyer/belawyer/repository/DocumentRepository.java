package com.lawyer.belawyer.repository;

import com.lawyer.belawyer.data.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    Optional<Document> findById(Long id);
}
