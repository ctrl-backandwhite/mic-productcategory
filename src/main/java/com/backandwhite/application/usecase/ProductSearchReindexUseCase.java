package com.backandwhite.application.usecase;

public interface ProductSearchReindexUseCase {

    /**
     * Deletes the index, recreates it and reindexes all products from PostgreSQL.
     * Use when the mapping changes or a full rebuild is needed.
     */
    long reindexAll();

    /**
     * Bulk upsert of all products from PostgreSQL without deleting the index.
     * Keeps existing documents and updates/adds according to the DB.
     */
    long reindexFromDb();
}
