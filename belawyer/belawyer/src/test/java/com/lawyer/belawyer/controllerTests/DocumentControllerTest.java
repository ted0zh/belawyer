package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.DocumentController;
import com.lawyer.belawyer.data.dto.DocumentSummaryDto;
import com.lawyer.belawyer.data.entity.Document;
import com.lawyer.belawyer.data.mapper.DocumentSummaryMapper;
import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    @Mock
    private DocumentServiceImpl documentService;

    @Mock
    private DocumentSummaryMapper mapper;

    @InjectMocks
    private DocumentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void upload_shouldReturn200AndDto() throws Exception {
        // Arrange
        byte[] fileBytes = "Hello World".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", fileBytes
        );
        Long caseId = 5L;

        Document savedDoc = new Document();
        savedDoc.setId(123L);
        savedDoc.setName("test.txt");
        savedDoc.setType("text/plain");
        savedDoc.setData(fileBytes);

        DocumentSummaryDto dto = new DocumentSummaryDto();
        dto.setId(123L);
        dto.setName("test.txt");
        dto.setType("text/plain");

        when(documentService.store(multipartFile, caseId)).thenReturn(savedDoc);
        when(mapper.toDto(savedDoc)).thenReturn(dto);

        // Act
        ResponseEntity<DocumentSummaryDto> response = controller.upload(multipartFile, caseId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(documentService, times(1)).store(multipartFile, caseId);
        verify(mapper, times(1)).toDto(savedDoc);
    }

    @Test
    void getDocumentSummaryById_found_returns200AndDto() {
        // Arrange
        Long docId = 10L;
        Document document = new Document();
        document.setId(docId);
        document.setName("file.pdf");
        document.setType("application/pdf");
        document.setData(new byte[]{0x01, 0x02});

        DocumentSummaryDto dto = new DocumentSummaryDto();
        dto.setId(docId);
        dto.setName("file.pdf");
        dto.setType("application/pdf");

        when(documentService.getDocumentEntityById(docId)).thenReturn(document);
        when(mapper.toDto(document)).thenReturn(dto);

        // Act
        ResponseEntity<DocumentSummaryDto> response = controller.getDocumentSummaryById(docId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(documentService, times(1)).getDocumentEntityById(docId);
        verify(mapper, times(1)).toDto(document);
    }

    @Test
    void getDocumentSummaryById_notFound_returns404() {
        // Arrange
        Long docId = 11L;
        when(documentService.getDocumentEntityById(docId)).thenReturn(null);

        // Act
        ResponseEntity<DocumentSummaryDto> response = controller.getDocumentSummaryById(docId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(documentService, times(1)).getDocumentEntityById(docId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void listByCase_returns200AndListOfDtos() {
        // Arrange
        Long caseId = 7L;
        DocumentSummaryDto dto1 = new DocumentSummaryDto();
        dto1.setId(100L);
        dto1.setName("a.docx");
        DocumentSummaryDto dto2 = new DocumentSummaryDto();
        dto2.setId(101L);
        dto2.setName("b.docx");
        List<DocumentSummaryDto> list = Arrays.asList(dto1, dto2);

        when(documentService.listByCaseId(caseId)).thenReturn(list);

        // Act
        ResponseEntity<List<DocumentSummaryDto>> response = controller.listByCase(caseId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());

        verify(documentService, times(1)).listByCaseId(caseId);
    }

    @Test
    void getSummary_returns200AndPlainText() {
        // Arrange
        Long docId = 20L;
        String summaryText = "This is a summary.";
        when(documentService.getSummary(docId)).thenReturn(summaryText);

        // Act
        ResponseEntity<String> response = controller.getSummary(docId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summaryText, response.getBody());

        verify(documentService, times(1)).getSummary(docId);
    }

    @Test
    void download_returns200AndFileBytesWithHeaders() {
        // Arrange
        Long docId = 30L;
        byte[] data = "PDFDATA".getBytes(StandardCharsets.UTF_8);
        Document doc = new Document();
        doc.setId(docId);
        doc.setName("report.pdf");
        doc.setType("application/pdf");
        doc.setData(data);

        when(documentService.getFile(docId)).thenReturn(doc);

        // Act
        ResponseEntity<byte[]> response = controller.download(docId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(data, response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.containsKey(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(
                "attachment; filename=\"report.pdf\"",
                headers.getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );
        assertEquals("application/pdf", headers.getContentType().toString());

        verify(documentService, times(1)).getFile(docId);
    }
}

