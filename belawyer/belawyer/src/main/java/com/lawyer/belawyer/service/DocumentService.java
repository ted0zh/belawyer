package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.dto.DocumentDto;
import com.lawyer.belawyer.data.entity.Document;

import java.util.List;
import java.util.Optional;

public interface DocumentService {
    Document uploadDocument(DocumentDto dto, Long caseId);
    DocumentDto getDocumentDetails(Long documentId);
    List<DocumentDto> getAllDocumentById(Long id);
    Optional<Document> getDocumentById(Long id);
    Document saveDocument(Document document);
}
