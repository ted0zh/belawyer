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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
            // 1. Create a temporary file for both Tika and Tesseract to read from
            tmp = File.createTempFile("upload-", ".tmp");
            //направи проверка за тип файл
            try (var out = new FileOutputStream(tmp)) {
                out.write(file.getBytes());
            }

            // 2. Detect MIME type via Apache Tika
            String mime = tika.detect(tmp);
            System.out.println("[OCR] Detected MIME type = " + mime);

            // 3. If it’s an image, attempt to load via ImageIO
            if (mime.startsWith("image")) {
                // 3.a) Try to read the file into a BufferedImage
                BufferedImage bi = ImageIO.read(tmp);
                if (bi == null) {
                    // ImageIO could not decode—no plugin recognized this format
                    throw new RuntimeException(
                            "Неподдържан формат на изображението (ImageIO.read() върна null). MIME=" + mime
                    );
                }

                // 3.b) Write the BufferedImage out as a PNG (always supported)
                File pngTmp = File.createTempFile("upload-converted-", ".png");
                try {
                    ImageIO.write(bi, "png", pngTmp);
                } catch (IOException e) {
                    // If writing to PNG fails for any reason, fall back to Tesseract on the original
                    return tesseract.doOCR(tmp).trim();
                }

                // 3.c) Run Tesseract OCR on the converted PNG
                String ocrResult = tesseract.doOCR(pngTmp).trim();
                pngTmp.delete();
                return ocrResult;
            }

            // 4. Otherwise (not an image), use Apache Tika to extract text from PDF, DOCX, etc.
            return extractWithTika(tmp);
        } catch (IOException | TesseractException e) {
            throw new RuntimeException("OCR/текстова екстракция неуспешна: " + e.getMessage(), e);
        } finally {
            // 5. Delete the temporary file
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        }
    }

    private String extractWithTika(File file) {
        var handler = new BodyContentHandler(-1);
        var metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        try (InputStream stream = java.nio.file.Files.newInputStream(file.toPath())) {
            parser.parse(stream, handler, metadata, new ParseContext());
            return cleanText(handler.toString());
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException("Грешка при Tika екстракция: " + e.getMessage(), e);
        }
    }
    private String cleanText(String raw) {
        String withNewlines = raw.replaceAll("\\r\\n?", "\n");
        return withNewlines.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "").trim();
    }
}
