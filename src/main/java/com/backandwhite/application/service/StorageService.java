package com.backandwhite.application.service;

import java.io.InputStream;

/**
 * Interface for the file storage service. Allows abstracting the storage
 * (local, S3, MinIO).
 */
public interface StorageService {

    /**
     * Stores a file and returns the generated (unique) filename.
     */
    String store(String originalFilename, String contentType, InputStream inputStream);

    /**
     * Stores a resized thumbnail and returns the filename.
     */
    String storeThumbnail(String originalFilename, String contentType, InputStream inputStream);

    /**
     * Deletes a file by name.
     */
    void delete(String filename);

    /**
     * Deletes a thumbnail by name.
     */
    void deleteThumbnail(String filename);

    /**
     * Returns the public URL for accessing a file.
     */
    String getUrl(String filename);

    /**
     * Returns the public thumbnail URL.
     */
    String getThumbnailUrl(String filename);

    /**
     * Gets the InputStream of a stored file.
     */
    InputStream load(String filename);
}
