package com.backandwhite.domain.repository;

public interface InventoryRepository {

    /**
     * Atomically decrement stock for a variant in a given country.
     *
     * @return number of rows updated (0 means insufficient stock)
     */
    int decrementStock(String vid, String countryCode, int quantity);

    /**
     * Atomically increment stock for a variant in a given country.
     *
     * @return number of rows updated
     */
    int incrementStock(String vid, String countryCode, int quantity);

    /**
     * Get total stock across all warehouses / countries for a variant.
     */
    int getTotalStockByVid(String vid);
}
