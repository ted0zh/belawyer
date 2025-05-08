package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.dto.DocumentDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.data.mapper.DocumentMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.DocumentRepository;
import com.lawyer.belawyer.service.DocumentService;
import org.springframework.stereotype.Service;


import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final DocumentMapper documentMapper;

    public DocumentServiceImpl(DocumentRepository documentRepository, CaseRepository caseRepository, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.documentMapper = documentMapper;
    }
    public Document uploadDocument(DocumentDto dto, Long caseId) {

        File file = new File(dto.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("File does not exist at path: " + dto.getFilePath());
        }

        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));

        Document document = documentMapper.toEntity(dto);
        document.setCaseEntity(legalCase);

        return documentRepository.save(document);
    }

    public DocumentDto getDocumentDetails(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        return new DocumentDto(doc.getFilePath(), doc.getCategory(), doc.getUploadedAt());
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<DocumentDto> getAllDocumentById(Long id) {
        Case legalCase = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        return legalCase.getDocuments().stream()
                .map(doc -> new DocumentDto(doc.getCategory(), doc.getUploadedAt()))
                .toList();
    }

    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public Optional<Case> getCaseById(Long id) {
        return caseRepository.findById(id);
    }
}

//    public Optional<Document> getDocumentById(Long id) {
//        return documentRepository.findById(id);
//    }
//    public List<DocumentDto> getAllDocumentById(Long id){
//        Optional<Case> caseOpt = caseRepository.findById(id);
//
//        Case legalCase = caseOpt.orElseThrow(()->new RuntimeException("No such document"));
//
//        return legalCase.getDocuments().stream()
//                .map(document -> new DocumentDto(document.getCategory(),document.getUploadedAt()))
//                .toList();
//    }
//
//    public Document saveDocument(Document document) {
//        return documentRepository.save(document);
//    }

