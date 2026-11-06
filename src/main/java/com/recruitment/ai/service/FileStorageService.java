package com.recruitment.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subFolder) {
        try {
            Path targetFolder = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            if (!Files.exists(targetFolder)) {
                Files.createDirectories(targetFolder);
            }

            String originalFilename = file.getOriginalFilename();
            String cleanName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "document.pdf";
            String uniqueName = UUID.randomUUID() + "_" + cleanName;
            Path targetPath = targetFolder.resolve(uniqueName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file: " + e.getMessage(), e);
        }
    }
}
