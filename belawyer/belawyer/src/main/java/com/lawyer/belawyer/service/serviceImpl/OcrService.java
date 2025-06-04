package com.lawyer.belawyer.service.serviceImpl;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class OcrService {
    private final ITesseract tesseract;
    private final Tika tika = new Tika();

    public OcrService(@Value("${tesseract.datapath}") String tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath);
        this.tesseract.setLanguage("bul");
    }
    public String extractText(MultipartFile file) {
        File tmp = null;
        try {
            // 1. Създаваме временен файл (за Tika и/или Tesseract)
            tmp = File.createTempFile("upload-", ".tmp");
            try (var out = new FileOutputStream(tmp)) {
                out.write(file.getBytes());
            }

            // 2. Определяме MIME тип чрез Tika
            String mime = tika.detect(tmp);

            // 3. Ако файлът е истинско изображение (jpg/png/gif/…),
            //    подаваме го на Tesseract за OCR
            if (mime.startsWith("image")) {
                return tesseract.doOCR(tmp).trim();
            }

            // 4. За всички други формати (PDF, DOCX, DOC, TXT и т.н.)
            //    използваме Apache Tika за извличане на текст
            return extractWithTika(tmp);
        } catch (IOException | TesseractException e) {
            throw new RuntimeException("OCR/текстова екстракция неуспешна: " + e.getMessage(), e);
        } finally {
            // 5. Изтриваме временния файл
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        }
    }

private String extractWithTika(File file) {
    // BodyContentHandler(null) ще се погрижи да върне цялото съдържание
    var handler = new BodyContentHandler(-1);
    var metadata = new Metadata();
    Parser parser = new AutoDetectParser();
    try (InputStream stream = java.nio.file.Files.newInputStream(file.toPath())) {
        parser.parse(stream, handler, metadata, new ParseContext());
        // Върни само текста, без контролни символи в излишък
        return cleanText(handler.toString());
    } catch (IOException | SAXException | TikaException e) {
        throw new RuntimeException("Грешка при Tika екстракция: " + e.getMessage(), e);
    }
}
    private String cleanText(String raw) {
        // Унифицираме новите редове
        String withNewlines = raw.replaceAll("\\r\\n?", "\n");
        // Премахваме всички контролни символи, освен \n и \t
        return withNewlines.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "").trim();
    }
}
