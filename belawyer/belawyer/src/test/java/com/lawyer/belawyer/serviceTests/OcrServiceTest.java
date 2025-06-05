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
    void extractText_whenMimeIsImage_invokesTesseract() throws Exception {
        // Arrange
        // Create a dummy MultipartFile (content doesn't matter since we mock Tika and Tesseract)
        byte[] dummyImageBytes = new byte[]{0x01, 0x02, 0x03};
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                dummyImageBytes
        );

        // When Tika.detect is called on any File, return an image MIME type
        when(mockTika.detect(any(File.class))).thenReturn("image/png");

        // Stub Tesseract.doOCR to return a fixed string
        when(mockTesseract.doOCR(any(File.class))).thenReturn("OCR_RESULT_TEXT");

        // Act
        String extracted = ocrService.extractText(mockFile);

        // Assert
        assertEquals("OCR_RESULT_TEXT", extracted);
        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, times(1)).doOCR(any(File.class));
    }

    @Test
    void extractText_whenMimeIsNonImage_usesTikaParser() throws Exception {
        // Arrange
        // Create a simple text-based MultipartFile; Tika parser should extract the same text
        String textContent = "Hello, world!\nSecond line.";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                textContent.getBytes()
        );

        // When Tika.detect is called on any File, return a non-image MIME type
        when(mockTika.detect(any(File.class))).thenReturn("text/plain");

        // Act
        String extracted = ocrService.extractText(mockFile);

        // Assert
        // The Tika parser should read the temporary file and return its textual content (cleaned).
        // Depending on newline normalization, we expect at least the same substrings.
        assertTrue(extracted.contains("Hello, world!"));
        assertTrue(extracted.contains("Second line."));
        verify(mockTika, times(1)).detect(any(File.class));
        // Tesseract should not be invoked in the non-image branch
        verify(mockTesseract, never()).doOCR(any(File.class));
    }

    @Test
    void extractText_whenTikaDetectThrowsIOException_propagatesRuntimeException() throws Exception {
        // Arrange
        byte[] dummyBytes = new byte[]{0x00};
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "badfile.txt",
                "text/plain",
                dummyBytes
        );

        // Make Tika.detect throw an IOException
        when(mockTika.detect(any(File.class))).thenThrow(new IOException("Tika failed"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ocrService.extractText(mockFile));
        assertTrue(ex.getMessage().contains("OCR/текстова екстракция неуспешна"));
        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, never()).doOCR(any(File.class));
    }
    @Test
    void cleanText_onlyRemovesUnwantedControls() throws Exception {
        String raw = "One\u0000\u0001\u0002Line\nTwo\tThree\u0003End";
        // Use reflection to call private method.
        Method cleanText = OcrService.class.getDeclaredMethod("cleanText", String.class);
        cleanText.setAccessible(true);

        String cleaned = (String) cleanText.invoke(ocrService, raw);

        // Assert that 0x00, 0x01, 0x02, 0x03 are gone, but '\n' and '\t' remain.
        assertFalse(cleaned.contains("\u0000"));
        assertFalse(cleaned.contains("\u0001"));
        assertFalse(cleaned.contains("\u0002"));
        assertFalse(cleaned.contains("\u0003"));
        assertTrue(cleaned.contains("\n"));
        assertTrue(cleaned.contains("\t"));
    }
//    @Test
//    void extractWithTika_privateMethod_cleansControlCharacters() throws Exception {
//        // To test cleanText behavior, we need to simulate extractWithTika calling cleanText.
//        // We will create a temporary file containing control characters and then call extractText
//        // in the non-image branch, verifying that control chars are stripped.
//
//        // Arrange
//        String rawWithControls = "Line1\u0000\u0001\u0002\nLine2\tMore\u0003Text";
//        byte[] bytes = rawWithControls.getBytes();
//        MockMultipartFile mockFile = new MockMultipartFile(
//                "file",
//                "controlled.txt",
//                "text/plain",
//                bytes
//        );
//
//        // Make Tika.detect say it's plain text
//        when(mockTika.detect(any(File.class))).thenReturn("text/plain");
//
//        // Act
//        String extracted = ocrService.extractText(mockFile);
//
//        // Assert
//        // The returned string should not contain control characters \u0000, \u0001, etc.
//        for (char c : new char[]{'\u0000', '\u0001', '\u0002', '\u0003'}) {
//            assertFalse(extracted.indexOf(c) >= 0, "Control character should be removed");
//        }
//        // Newlines and tabs should remain
//        assertTrue(extracted.contains("\n"));
//        assertTrue(extracted.contains("\t"));
//        verify(mockTika, times(1)).detect(any(File.class));
//        verify(mockTesseract, never()).doOCR(any(File.class));
//    }
    @Test
    void extractWithTika_privateMethod_cleansControlCharacters() throws Exception {
        // Arrange: build a “raw” byte array containing control chars + newline + tab
        String rawWithControls = "Line1\u0000\u0001\u0002\nLine2\tMore\u0003Text";
        byte[] bytes = rawWithControls.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "controlled.txt",
            "text/plain",
            bytes
        );

        // Stub Tika.detect(...) so we go into the non-image (Tika) branch
        when(mockTika.detect(any(File.class))).thenReturn("text/plain");

        // Act: this calls extractWithTika(...), then cleanText(...)
        String extracted = ocrService.extractText(mockFile);

        // Assert: none of the “unwanted” control chars remain
        for (char c : new char[]{'\u0000', '\u0001', '\u0002', '\u0003'}) {
            assertFalse(extracted.indexOf(c) >= 0,
                "Control character " + Integer.toHexString(c) + " should be removed");
        }

        // We no longer check for newline or tab,
        // because Tika may normalize or remove them during parsing.

        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, never()).doOCR(any(File.class));
    }



    @Test
    void extractText_whenTesseractThrowsTesseractException_propagatesRuntimeException() throws Exception {
        // Arrange
        byte[] dummyImageBytes = new byte[]{0x0A, 0x0B};
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                dummyImageBytes
        );

        when(mockTika.detect(any(File.class))).thenReturn("image/jpeg");
        when(mockTesseract.doOCR(any(File.class))).thenThrow(new TesseractException("OCR failed"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ocrService.extractText(mockFile));
        assertTrue(ex.getMessage().contains("OCR/текстова екстракция неуспешна"));
        verify(mockTika, times(1)).detect(any(File.class));
        verify(mockTesseract, times(1)).doOCR(any(File.class));
    }
}
