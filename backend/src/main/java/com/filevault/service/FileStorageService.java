package com.filevault.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.storage.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize root file storage directory.", ex);
        }
    }

    public String storeFile(MultipartFile file, UUID userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User identity is required for file storage.");
        }

        String safeExtension = extractSafeExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + (safeExtension.isEmpty() ? "" : "." + safeExtension);

        Path userDir = resolveAndValidateUserDirectory(userId);
        Path targetLocation = userDir.resolve(storedFileName).normalize();

        if (!targetLocation.startsWith(userDir) || !targetLocation.startsWith(this.rootLocation)) {
            throw new SecurityException("Path traversal attempt blocked.");
        }

        try {
            Files.createDirectories(userDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file on disk.", ex);
        }
    }

    public Resource loadFileAsResource(UUID userId, String storedName) {
        Path filePath = resolveValidatedFilePath(userId, storedName);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new NoSuchElementException("Requested file resource does not exist on storage.");
        } catch (MalformedURLException ex) {
            throw new NoSuchElementException("Requested file resource could not be resolved.");
        }
    }

    public void deletePhysicalFile(UUID userId, String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        Path filePath = resolveValidatedFilePath(userId, storedName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete physical file from storage.", ex);
        }
    }

    public String sanitizeOriginalFilename(String rawFilename) {
        if (rawFilename == null || rawFilename.isBlank()) {
            return "untitled-file";
        }
        // Strip any directory paths (Unix or Windows) and control characters
        String baseName = rawFilename.replace('\\', '/');
        int lastSlash = baseName.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }
        baseName = baseName.replaceAll("[\\r\\n\\t\\u0000]", "").trim();
        if (baseName.isEmpty() || ".".equals(baseName) || "..".equals(baseName)) {
            return "untitled-file";
        }
        return baseName.length() > 240 ? baseName.substring(baseName.length() - 240) : baseName;
    }

    private Path resolveAndValidateUserDirectory(UUID userId) {
        Path userDir = this.rootLocation.resolve(userId.toString()).normalize();
        if (!userDir.startsWith(this.rootLocation)) {
            throw new SecurityException("Invalid user storage directory.");
        }
        return userDir;
    }

    private Path resolveValidatedFilePath(UUID userId, String storedName) {
        if (storedName == null || storedName.contains("..") || storedName.contains("/") || storedName.contains("\\")) {
            throw new SecurityException("Invalid stored file identifier.");
        }
        Path userDir = resolveAndValidateUserDirectory(userId);
        Path targetPath = userDir.resolve(storedName).normalize();
        if (!targetPath.startsWith(userDir) || !targetPath.startsWith(this.rootLocation)) {
            throw new SecurityException("Path traversal attempt blocked.");
        }
        return targetPath;
    }

    private String extractSafeExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        String candidate = filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        if (candidate.matches("^[a-z0-9]{1,12}$")) {
            return candidate;
        }
        return "";
    }
}
