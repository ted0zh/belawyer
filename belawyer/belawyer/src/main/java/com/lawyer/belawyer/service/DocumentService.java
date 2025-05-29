package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.entity.Document;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


public interface DocumentService {
    List<Document> listByCase(Long caseId);
    Document store(MultipartFile file,Long caseId) throws IOException;
    Document getFile(Long id);
    Stream<Document> getAllFiles();
    String getSummary(Long documentId);
}
