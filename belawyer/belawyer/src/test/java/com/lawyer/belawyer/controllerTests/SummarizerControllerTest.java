package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.SummarizerController;
import com.lawyer.belawyer.service.TextSummarizer;
import com.lawyer.belawyer.service.serviceImpl.OcrService;
import com.lawyer.belawyer.service.serviceImpl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SummarizerControllerTest {

    @Mock
    private TextSummarizer summarizer;

    @Mock
    private OcrService ocrService;

    @Mock
    private DocumentServiceImpl documentService; // Not used by the endpoint but required by constructor

    @InjectMocks
    private SummarizerController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void summarize_withCustomSentenceCount_invokesOcrAndSummarizer() throws Exception {
        // Arrange
        byte[] fileBytes = "dummy content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", fileBytes
        );
        int sentenceCount = 5;
        String extractedText = "Extracted text from file";
        List<String> expectedSummary = Arrays.asList("Sentence 1", "Sentence 2", "Sentence 3");

        when(ocrService.extractText(file)).thenReturn(extractedText);
        when(summarizer.summarize(extractedText, sentenceCount)).thenReturn(expectedSummary);

        // Act
        ResponseEntity<List<String>> response = controller.summarize(file, sentenceCount);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedSummary, response.getBody());

        verify(ocrService, times(1)).extractText(file);
        verify(summarizer, times(1)).summarize(extractedText, sentenceCount);
        verifyNoMoreInteractions(ocrService, summarizer, documentService);
    }

    @Test
    void summarize_withDefaultSentenceCount_invokesOcrAndSummarizerWithThree() throws Exception {
        // Arrange
        byte[] fileBytes = "another dummy".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", fileBytes
        );
        String extractedText = "Some other extracted text";
        List<String> expectedSummary = Arrays.asList("A", "B", "C");

        // Simulate defaultValue="3" by passing 3 explicitly in unit test
        int defaultSentences = 3;

        when(ocrService.extractText(file)).thenReturn(extractedText);
        when(summarizer.summarize(extractedText, defaultSentences)).thenReturn(expectedSummary);

        // Act
        ResponseEntity<List<String>> response = controller.summarize(file, defaultSentences);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedSummary, response.getBody());

        verify(ocrService, times(1)).extractText(file);
        verify(summarizer, times(1)).summarize(extractedText, defaultSentences);
        verifyNoMoreInteractions(ocrService, summarizer, documentService);
    }
}
