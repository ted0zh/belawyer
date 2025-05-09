package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.entity.Document;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.stream.Stream;


public interface DocumentService {

    public Document store(MultipartFile file,Long caseId) throws IOException;
    public Document getFile(Long id);
    Stream<Document> getAllFiles();
}
