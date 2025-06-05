package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.data.dto.DocumentSummaryDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.data.mapper.DocumentSummaryMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.DocumentRepository;
import com.lawyer.belawyer.service.serviceImpl.TextRankSummarizer;
import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import com.lawyer.belawyer.service.serviceImpl.OcrService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.persistence.EntityNotFoundException;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private TextRankSummarizer summarizer;

    @Mock
    private DocumentSummaryMapper summaryMapper;

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void testStore_successfulSave() throws Exception {
        // Arrange
        Long caseId = 1L;
        String filename = "test.txt";
        String contentType = "text/plain";
        byte[] fileBytes = "This is the full text of the document.".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                filename,
                contentType,
                fileBytes
        );

        Case mockCase = new Case();
        mockCase.setId(caseId);

        String extractedText = "Full text extracted by OCR.";
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        when(ocrService.extractText(multipartFile)).thenReturn(extractedText);

        List<String> summarySentences = Arrays.asList("Sentence one.", "Sentence two.", "Sentence three.");
        when(summarizer.summarize(extractedText, 3)).thenReturn(summarySentences);

        Document toSave = new Document();
        // The repository.save should return a Document with an ID set
        Document savedDocument = new Document();
        savedDocument.setId(42L);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document arg = invocation.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        // Act
        Document result = documentService.store(multipartFile, caseId);

        // Assert
        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals(filename, result.getName());
        assertEquals(contentType, result.getType());
        assertArrayEquals(fileBytes, result.getData());
        assertEquals(mockCase, result.getCaseEntity());
        String expectedSummary = String.join(" ", summarySentences);
        assertEquals(expectedSummary, result.getSummary());

        verify(caseRepository, times(1)).findById(caseId);
        verify(ocrService, times(1)).extractText(multipartFile);
        verify(summarizer, times(1)).summarize(extractedText, 3);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testStore_caseNotFound_throwsException() {
        // Arrange
        Long caseId = 999L;
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "ignored.txt",
                "text/plain",
                "irrelevant".getBytes()
        );
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                documentService.store(multipartFile, caseId)
        );
        assertTrue(ex.getMessage().contains("Case not found: " + caseId));

        verify(caseRepository, times(1)).findById(caseId);
        verifyNoMoreInteractions(ocrService, summarizer, documentRepository);
    }

    @Test
    void testListByCaseId_returnsMappedDtos() {
        // Arrange
        Long caseId = 2L;
        Document doc1 = new Document();
        doc1.setId(10L);
        Document doc2 = new Document();
        doc2.setId(11L);
        List<Document> docs = Arrays.asList(doc1, doc2);

        DocumentSummaryDto dto1 = new DocumentSummaryDto();
        DocumentSummaryDto dto2 = new DocumentSummaryDto();
        when(documentRepository.findByCaseEntityId(caseId)).thenReturn(docs);
        when(summaryMapper.toDto(doc1)).thenReturn(dto1);
        when(summaryMapper.toDto(doc2)).thenReturn(dto2);

        // Act
        List<DocumentSummaryDto> result = documentService.listByCaseId(caseId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(dto1, dto2)));

        verify(documentRepository, times(1)).findByCaseEntityId(caseId);
        verify(summaryMapper, times(1)).toDto(doc1);
        verify(summaryMapper, times(1)).toDto(doc2);
    }

    @Test
    void testListByCase_returnsDocuments() {
        // Arrange
        Long caseId = 3L;
        Document doc = new Document();
        doc.setId(15L);
        when(documentRepository.findByCaseEntityId(caseId)).thenReturn(Collections.singletonList(doc));

        // Act
        List<Document> result = documentService.listByCase(caseId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(doc, result.get(0));

        verify(documentRepository, times(1)).findByCaseEntityId(caseId);
    }

    @Test
    void testGetFile_found() {
        // Arrange
        Long docId = 20L;
        Document doc = new Document();
        doc.setId(docId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Act
        Document result = documentService.getFile(docId);

        // Assert
        assertEquals(doc, result);
        verify(documentRepository, times(1)).findById(docId);
    }

    @Test
    void testGetFile_notFound_throwsException() {
        // Arrange
        Long docId = 21L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                documentService.getFile(docId)
        );
        assertTrue(ex.getMessage().contains("Document not found: " + docId));

        verify(documentRepository, times(1)).findById(docId);
    }

    @Test
    void testGetAllFiles_returnsStreamContents() {
        // Arrange
        Document d1 = new Document();
        d1.setId(30L);
        Document d2 = new Document();
        d2.setId(31L);
        when(documentRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        // Act
        List<Document> result = documentService.getAllFiles().collect(Collectors.toList());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(d1));
        assertTrue(result.contains(d2));

        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void testSummarizePdf_successfulPdfSummarization() throws Exception {
        // Arrange
        // Create a simple in-memory PDF with one line of text
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(pdf, page);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(25, 700);
        contentStream.showText("This is a test PDF for summarization.");
        contentStream.endText();
        contentStream.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdf.save(baos);
        pdf.close();

        byte[] pdfBytes = baos.toByteArray();
        MockMultipartFile mockPdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                pdfBytes
        );

        List<String> summarySentences = Arrays.asList("Summary sentence 1.", "Summary sentence 2.");
        // Stub summarizer to return our test sentences, ignoring the actual text
        when(summarizer.summarize(anyString(), eq(2))).thenReturn(summarySentences);

        // Act
        List<String> result = documentService.summarizePdf(mockPdfFile, 2);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Summary sentence 1.", result.get(0));
        assertEquals("Summary sentence 2.", result.get(1));

        verify(summarizer, times(1)).summarize(any(String.class), eq(2));
    }

    @Test
    void testGetSummary_updatesAndReturnsSummary() throws Exception {
        // Arrange
        Long docId = 50L;
        byte[] dummyData = "Plain text inside PDF".getBytes();
        Document existingDoc = new Document();
        existingDoc.setId(docId);
        existingDoc.setData(dummyData);
        existingDoc.setSummary("Old summary");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(existingDoc));

        // We want to verify clearSummary is called, so we stub it as a no-op
        doNothing().when(documentRepository).clearSummary(docId);

        List<String> summarySentences = Arrays.asList("First line of summary.", "Second line.");
        when(summarizer.summarize(anyString(), eq(3))).thenReturn(summarySentences);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        when(documentRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String returnedSummary = documentService.getSummary(docId);

        // Assert
        String expectedCombined = String.join(" ", summarySentences);
        assertEquals(expectedCombined, returnedSummary);

        verify(documentRepository, times(1)).clearSummary(docId);
        verify(documentRepository, times(1)).findById(docId);
        verify(summarizer, times(1)).summarize(anyString(), eq(3));
        verify(documentRepository, times(1)).save(any(Document.class));

        Document savedDoc = captor.getValue();
        assertEquals(expectedCombined, savedDoc.getSummary());
    }

    @Test
    void testGetSummary_documentNotFound_throwsException() {
        // Arrange
        Long docId = 60L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                documentService.getSummary(docId)
        );
        assertTrue(ex.getMessage().contains("Document not found: " + docId));

        verify(documentRepository, times(1)).clearSummary(docId);
        verify(documentRepository, times(1)).findById(docId);
        verifyNoMoreInteractions(summarizer, documentRepository);
    }

    @Test
    void testGetDocumentEntityById_found() {
        // Arrange
        Long docId = 70L;
        Document doc = new Document();
        doc.setId(docId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Act
        Document result = documentService.getDocumentEntityById(docId);

        // Assert
        assertEquals(doc, result);
        verify(documentRepository, times(1)).findById(docId);
    }

    @Test
    void testGetDocumentEntityById_notFound_throwsNoSuchElementException() {
        // Arrange
        Long docId = 71L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(java.util.NoSuchElementException.class, () ->
                documentService.getDocumentEntityById(docId)
        );
        verify(documentRepository, times(1)).findById(docId);
    }
}

