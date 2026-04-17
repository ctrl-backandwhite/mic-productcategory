package com.backandwhite.application.port.out;

public interface CatalogEventPort {

    // ── Product events ──────────────────────────────────────────────────────

    void publishProductCreated(String productId, String name, String sku, String price, String categoryId,
            String brandId);

    void publishProductUpdated(String productId, String name, String price, String categoryId, String brandId,
            boolean enabled);

    void publishProductStateChanged(String productId, String name, boolean enabled);

    // ── Category events ─────────────────────────────────────────────────────

    void publishCategoryUpdated(String categoryId, String name, String parentId, boolean enabled);

    // ── Stock events ────────────────────────────────────────────────────────

    void publishStockReserved(String productId, String variantId, String orderId, int quantity, int remainingStock);

    void publishStockDeducted(String productId, String variantId, String orderId, int quantity, int remainingStock);

    void publishStockReleased(String productId, String variantId, String orderId, int quantity, int remainingStock);

    void publishStockDepleted(String productId, String variantId, String productName);

    void publishStockRestored(String productId, String variantId, String orderId, int quantity, int remainingStock);

    void publishStockLowAlert(String productId, String variantId, String productName, int currentStock, int threshold);
}
