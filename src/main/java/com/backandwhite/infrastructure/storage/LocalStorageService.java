package com.backandwhite.infrastructure.storage;

import com.backandwhite.application.service.StorageService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * StorageService implementation for local filesystem storage. In production it
 * is replaced by an S3/MinIO adapter.
 */
@Log4j2
@Service
public class LocalStorageService implements StorageService {

    @Value("${media.storage.path:./uploads}")
    private String storagePath;

    @Value("${media.storage.thumbnail-path:./uploads/thumbnails}")
    private String thumbnailPath;

    @Value("${media.storage.base-url:/api/v1/media/images}")
    private String baseUrl;

    private Path rootLocation;
    private Path thumbnailLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        thumbnailLocation = Paths.get(thumbnailPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(thumbnailLocation);
            log.info("Storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directories", e);
        }
    }

    @Override
    public String store(String originalFilename, String contentType, InputStream inputStream) {
        String filename = generateUniqueFilename(originalFilename);
        try {
            Path destinationFile = rootLocation.resolve(filename).normalize();
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {} (original: {})", filename, originalFilename);
            return filename;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public String storeThumbnail(String originalFilename, String contentType, InputStream inputStream) {
        String filename = "thumb_" + generateUniqueFilename(originalFilename);
        try {
            Path destinationFile = thumbnailLocation.resolve(filename).normalize();
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored thumbnail: {}", filename);
            return filename;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store thumbnail: " + originalFilename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("Deleted file: {}", filename);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filename, e);
        }
    }

    @Override
    public void deleteThumbnail(String filename) {
        try {
            Path file = thumbnailLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("Deleted thumbnail: {}", filename);
        } catch (IOException e) {
            log.warn("Failed to delete thumbnail: {}", filename, e);
        }
    }

    @Override
    public String getUrl(String filename) {
        return baseUrl + "/" + filename;
    }

    @Override
    public String getThumbnailUrl(String filename) {
        return baseUrl + "/thumbnails/" + filename;
    }

    @Override
    public InputStream load(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file: " + filename, e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}
