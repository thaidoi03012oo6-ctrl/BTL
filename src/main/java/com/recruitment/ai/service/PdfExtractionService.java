package com.recruitment.ai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfExtractionService {

    public String extractTextFromPdf(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File không tồn tại tại đường dẫn: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            if (document.isEncrypted()) {
                throw new IllegalStateException("Không thể đọc file PDF có mật khẩu bảo vệ!");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            return cleanExtractedText(rawText);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file PDF: " + e.getMessage(), e);
        }
    }

    private String cleanExtractedText(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\r\\t]+", " ")
                   .replaceAll(" +", " ")
                   .replaceAll("(\\n ?){3,}", "\n\n")
                   .trim();
    }
}
