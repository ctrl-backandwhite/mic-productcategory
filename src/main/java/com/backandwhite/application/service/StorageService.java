package com.backandwhite.application.service;

import java.io.InputStream;

/**
 * Interfaz para el servicio de almacenamiento de archivos.
 * Permite abstraer el almacenamiento (local, S3, MinIO).
 */
public interface StorageService {

    /**
     * Almacena un archivo y devuelve el nombre de archivo generado (único).
     */
    String store(String originalFilename, String contentType, InputStream inputStream);

    /**
     * Almacena un thumbnail redimensionado y devuelve el nombre de archivo.
     */
    String storeThumbnail(String originalFilename, String contentType, InputStream inputStream);

    /**
     * Elimina un archivo por nombre.
     */
    void delete(String filename);

    /**
     * Elimina un thumbnail por nombre.
     */
    void deleteThumbnail(String filename);

    /**
     * Devuelve la URL pública para acceder a un archivo.
     */
    String getUrl(String filename);

    /**
     * Devuelve la URL pública del thumbnail.
     */
    String getThumbnailUrl(String filename);

    /**
     * Obtiene el InputStream de un archivo almacenado.
     */
    InputStream load(String filename);
}
