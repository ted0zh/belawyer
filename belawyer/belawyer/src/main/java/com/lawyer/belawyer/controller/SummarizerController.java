package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import com.lawyer.belawyer.service.serviceImpl.OcrService;
import com.lawyer.belawyer.service.TextSummarizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class SummarizerController {
    private final TextSummarizer summarizer;
    private final OcrService ocr;
    private final DocumentServiceImpl documentService;

    public SummarizerController(TextSummarizer summarizer, OcrService ocr, DocumentServiceImpl documentService) {
        this.summarizer = summarizer;
        this.ocr = ocr;
        this.documentService = documentService;
    }
    //get a summary when file is uploaded
    @PostMapping("/api/v1/summarize/file")
    public ResponseEntity<List<String>> summarize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue="3") int sentences) {
        String text = ocr.extractText(file);
        var summary = summarizer.summarize(text, sentences);

        return ResponseEntity.ok(summary);
    }
}
