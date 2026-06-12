package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.config.StorageProperties;
import com.DB.DBmarket.pojo.storage.StoredObject;
import com.DB.DBmarket.service.ObjectStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalObjectStorageService implements ObjectStorageService {
    @Resource
    private StorageProperties storageProperties;

    @Override
    public StoredObject store(MultipartFile file, String category) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String normalizedCategory = normalizeCategory(category);
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(extension)) {
            fileName = fileName + "." + extension.toLowerCase(Locale.ROOT);
        }

        LocalDate today = LocalDate.now();
        Path rootDir = Paths.get(storageProperties.getLocal().getBaseDir()).toAbsolutePath().normalize();
        Path categoryDir = rootDir.resolve(normalizedCategory).resolve(today.toString());
        Files.createDirectories(categoryDir);

        Path targetFile = categoryDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = normalizedCategory + "/" + today + "/" + fileName;
        String publicUrlPrefix = storageProperties.getLocal().getPublicUrlPrefix().replaceAll("/+$", "");
        return new StoredObject(
                relativePath,
                publicUrlPrefix + "/" + relativePath,
                file.getSize(),
                file.getContentType()
        );
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return "common";
        }
        return category.replaceAll("[^a-zA-Z0-9/_-]", "_");
    }
}
