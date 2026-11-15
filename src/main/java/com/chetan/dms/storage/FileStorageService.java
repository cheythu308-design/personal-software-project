package com.chetan.dms.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(@Value("${dms.storage.location:./data/documents}") String location) {
        this.storageRoot = Path.of(location).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new FileStorageException("Unable to initialize document storage.", exception);
        }
    }

    public String store(InputStream inputStream, String originalFilename) {
        String extension = extensionOf(originalFilename);
        String storageKey = UUID.randomUUID() + extension;
        Path destination = resolve(storageKey);
        try (InputStream input = inputStream) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            return storageKey;
        } catch (IOException exception) {
            throw new FileStorageException("Unable to store the uploaded document.", exception);
        }
    }

    public Resource loadAsResource(String storageKey) {
        Path file = resolve(storageKey);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new FileStorageException("The stored document file is missing or unreadable.");
        } catch (IOException exception) {
            throw new FileStorageException("Unable to read the stored document file.", exception);
        }
    }

    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new FileStorageException("Unable to delete the stored document file.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = storageRoot.resolve(storageKey).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new FileStorageException("Invalid document storage key.");
        }
        return resolved;
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        String safeName = Path.of(filename.replace("\\", "/")).getFileName().toString();
        int dot = safeName.lastIndexOf('.');
        if (dot <= 0 || dot == safeName.length() - 1) {
            return "";
        }
        String extension = safeName.substring(dot).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
    }
}
