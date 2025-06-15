package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.service.serviceImpl.OcrService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class OcrServiceTest {

    private OcrService ocrService;
    private Tika mockTika;
    private ITesseract mockTesseract;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the service (the Tesseract datapath is irrelevant for testing because we will overwrite)
        ocrService = new OcrService("/dummy/path/to/tessdata");

        // Create mocks
        mockTika = Mockito.mock(Tika.class);
        mockTesseract = Mockito.mock(ITesseract.class);

        // Inject the mock Tika into the private final field 'tika'
        Field tikaField = OcrService.class.getDeclaredField("tika");
        tikaField.setAccessible(true);
        tikaField.set(ocrService, mockTika);

        // Inject the mock ITesseract into the private final field 'tesseract'
        Field tessField = OcrService.class.getDeclaredField("tesseract");
        tessField.setAccessible(true);
        tessField.set(ocrService, mockTesseract);
    }
    @Test
    void extractText_whenMimeIsNonImage_usesTikaParser() throws Exception {
        String textContent = "Hello, world!\nSecond line.";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                textContent.getBytes()
        );

        when(mockTika.detect(any(File.class))).thenReturn("text/plain");

        String extracted = ocrService.extractText(mockFile);

        assertTrue(extracted.contains("Hello, world!"));
        assertTrue(extracted.contains("Second line."));
        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, never()).doOCR(any(File.class));
    }

    @Test
    void extractText_whenTikaDetectThrowsIOException_propagatesRuntimeException() throws Exception {
        byte[] dummyBytes = new byte[]{0x00};
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "badfile.txt",
                "text/plain",
                dummyBytes
        );

        when(mockTika.detect(any(File.class))).thenThrow(new IOException("Tika failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ocrService.extractText(mockFile));
        assertTrue(ex.getMessage().contains("OCR/текстова екстракция неуспешна"));
        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, never()).doOCR(any(File.class));
    }
    @Test
    void cleanText_onlyRemovesUnwantedControls() throws Exception {
        String raw = "One\u0000\u0001\u0002Line\nTwo\tThree\u0003End";
        Method cleanText = OcrService.class.getDeclaredMethod("cleanText", String.class);
        cleanText.setAccessible(true);

        String cleaned = (String) cleanText.invoke(ocrService, raw);

        assertFalse(cleaned.contains("\u0000"));
        assertFalse(cleaned.contains("\u0001"));
        assertFalse(cleaned.contains("\u0002"));
        assertFalse(cleaned.contains("\u0003"));
        assertTrue(cleaned.contains("\n"));
        assertTrue(cleaned.contains("\t"));
    }

    @Test
    void extractWithTika_privateMethod_cleansControlCharacters() throws Exception {
        String rawWithControls = "Line1\u0000\u0001\u0002\nLine2\tMore\u0003Text";
        byte[] bytes = rawWithControls.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "controlled.txt",
            "text/plain",
            bytes
        );

        when(mockTika.detect(any(File.class))).thenReturn("text/plain");

        String extracted = ocrService.extractText(mockFile);

        for (char c : new char[]{'\u0000', '\u0001', '\u0002', '\u0003'}) {
            assertFalse(extracted.indexOf(c) >= 0,
                "Control character " + Integer.toHexString(c) + " should be removed");
        }

        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, never()).doOCR(any(File.class));
    }
}
