package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.DocumentRepository;
import com.lawyer.belawyer.service.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final Path root = Paths.get("uploads");

    public DocumentServiceImpl(DocumentRepository documentRepository, CaseRepository caseRepository) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;

    }




    public Document store(MultipartFile file,Long caseId) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Optional<Case> legalCase = caseRepository.findById(caseId);
        Document document = new Document(fileName, file.getContentType(), file.getBytes());
        document.setCaseEntity(legalCase.get());

        return documentRepository.save(document);
    }

    public Document getFile(Long id) {
        return documentRepository.findById(id).get();
    }

    public Stream<Document> getAllFiles() {
        return documentRepository.findAll().stream();
    }
}




