package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.DocumentDto;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/document")
public class DocumentController {
    private final DocumentServiceImpl documentService;

    public DocumentController(DocumentServiceImpl documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> upload(@RequestBody DocumentDto dto, @RequestParam Long id) {
        Document document = documentService.uploadDocument(dto, id);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/get")
    public ResponseEntity<DocumentDto> get(@RequestParam Long id) {
        DocumentDto dto = documentService.getDocumentDetails(id);
        return ResponseEntity.ok(dto);
    }
}
