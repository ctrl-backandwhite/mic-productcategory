package com.backandwhite.application.usecase;

public interface InventoryUseCase {

    /**
     * Reserve stock temporarily for an order (before payment confirmation).
     * 
     * @return remaining stock after reservation
     */
    int reserveStock(String variantId, String orderId, int quantity, String countryCode);

    /**
     * Deduct stock permanently after payment confirmed.
     * 
     * @return remaining stock after deduction
     */
    int deductStock(String variantId, String orderId, int quantity, String countryCode);

    /**
     * Release previously reserved stock (order cancelled/expired).
     * 
     * @return remaining stock after release
     */
    int releaseStock(String variantId, String orderId, int quantity, String countryCode);

    /**
     * Restore stock from a return.
     * 
     * @return remaining stock after restore
     */
    int restoreStock(String variantId, String orderId, int quantity, String countryCode);

    /**
     * Check available stock for a variant across all warehouses.
     */
    int getAvailableStock(String variantId);

    /**
     * Check if a variant has enough stock.
     */
    boolean hasStock(String variantId, int quantity);
}
