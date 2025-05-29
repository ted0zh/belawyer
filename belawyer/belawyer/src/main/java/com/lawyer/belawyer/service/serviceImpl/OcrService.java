package com.lawyer.belawyer.service.serviceImpl;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class OcrService {
    private final ITesseract tesseract;
    private final Tika tika = new Tika();

    public OcrService(@Value("${tesseract.datapath}") String tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath);
    }

    public String extractText(MultipartFile file) {
        try {
            File tmp = File.createTempFile("upload","tmp");
            try (var out = new FileOutputStream(tmp)) {
                out.write(file.getBytes());
            }
            String mime = tika.detect(tmp);
            if (!mime.startsWith("image")) {
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            return tesseract.doOCR(tmp);
        } catch (IOException|TesseractException e) {
            throw new RuntimeException("OCR failed",e);
        }
    }
}
