package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.DocumentSummaryDto;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.data.mapper.DocumentSummaryMapper;
import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentServiceImpl documentService;
    private final DocumentSummaryMapper mapper;

    public DocumentController(DocumentServiceImpl documentService, DocumentSummaryMapper mapper) {
        this.documentService = documentService;
        this.mapper = mapper;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentSummaryDto> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("caseId") Long caseId) {
        var saved = documentService.store(file, caseId);
        var dto = mapper.toDto(saved);
        return ResponseEntity.ok(dto);
    }

    // Endpoint to get a single DocumentSummaryDto by ID
    @GetMapping("/{id}") // Map to /api/v1/documents/{id}
    public ResponseEntity<DocumentSummaryDto> getDocumentSummaryById(@PathVariable Long id) {
        // You need a service method that fetches a Document entity by its ID
        // and then maps it to a DocumentSummaryDto.
        // Assuming documentService has a method like getDocumentEntityById(Long id)
        Document document = documentService.getDocumentEntityById(id); // Implement this service method

        if (document != null) {
            DocumentSummaryDto dto = mapper.toDto(document); // Ensure this mapper includes caseId
            return ResponseEntity.ok(dto);
        } else {
            // Return 404 Not Found if document is not found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/bycase/{caseId}")
    public ResponseEntity<List<DocumentSummaryDto>> listByCase(@PathVariable Long caseId) {
//        var list = documentService.listByCase(caseId).stream()
//                .map(mapper::toDto)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(list);
        List<DocumentSummaryDto> summaries = documentService.listByCaseId(caseId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping(
            path = "/summary/{id}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getSummary(@PathVariable Long id) {
        String summary = documentService.getSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        var doc = documentService.getFile(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + doc.getName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getType()))
                .body(doc.getData());
    }

}



