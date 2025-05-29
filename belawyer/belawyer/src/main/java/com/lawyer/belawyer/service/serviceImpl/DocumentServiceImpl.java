package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.data.mapper.DocumentSummaryMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.DocumentRepository;
import com.lawyer.belawyer.service.DocumentService;
import com.lawyer.belawyer.service.TextSummarizer;
import jakarta.persistence.EntityNotFoundException;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final TextRankSummarizer summarizer;
    private final DocumentSummaryMapper summaryMapper;
    private final AutoDetectParser parser = new AutoDetectParser();
    private final Tika tika = new Tika();
    private final OcrService ocrService;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               CaseRepository caseRepository,
                               TextRankSummarizer summarizer,
                               DocumentSummaryMapper summaryMapper,
                               OcrService ocrService) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.summarizer = summarizer;
        this.summaryMapper = summaryMapper;
        this.ocrService = ocrService;
    }

    @Override
    public Document store(MultipartFile file, Long caseId) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));
        try {
            String fullText = ocrService.extractText(file);
            List<String> summarySentences = summarizer.summarize(fullText, 3);
            String summary = String.join(" ", summarySentences).replace("\0", ""); // remove null bytes

            Document doc = new Document();
            doc.setName(file.getOriginalFilename());
            doc.setType(file.getContentType());
            doc.setData(file.getBytes());
            doc.setCaseEntity(legalCase);
            doc.setSummary(summary);

            return documentRepository.save(doc);
        } catch (Exception e) {
            throw new RuntimeException("Error processing file", e);
        }
    }

    @Override
    public List<Document> listByCase(Long caseId) {
        return documentRepository.findByCaseEntityId(caseId);
    }

    @Override
    public Document getFile(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

    @Override
    public Stream<Document> getAllFiles() {
        return documentRepository.findAll().stream();
    }

    public List<String> summarizePdf(MultipartFile file, int numSentences) {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return summarizer.summarize(text, numSentences);
        } catch (IOException e) {
            throw new UncheckedIOException("Неуспешно четене на PDF", e);
        }
    }


    @Override
    @Transactional
    public String getSummary(Long documentId) {
        documentRepository.clearSummary(documentId);
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document not found: " + documentId));

        try (InputStream in = new ByteArrayInputStream(doc.getData())) {
            // събираме пълен текст
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            parser.parse(in, handler, metadata, context);

            String fullText = handler.toString()
                    .replace("\0", "")     // махаме null-char, ако има
                    .trim();

            // пускаме TextRankSummarizer за 3 изречения
            List<String> summarySentences = summarizer.summarize(fullText, 3);
            String summary = String.join(" ", summarySentences);

            // запазваме summary-то в базата
            doc.setSummary(summary);
            documentRepository.save(doc);

            return summary;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error extracting text from PDF", e);
        }
    }
}




