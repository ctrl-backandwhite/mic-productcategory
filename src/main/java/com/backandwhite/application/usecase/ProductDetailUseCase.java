package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Caso de uso para obtener el detalle de un producto.
 * Si el producto no existe en la BD local, lo obtiene desde CJ Dropshipping,
 * lo persiste y luego lo devuelve desde la BD.
 */
public interface ProductDetailUseCase {

    /**
     * Obtiene el detalle de un producto por su pid (CJ product ID).
     * <ol>
     * <li>Busca el producto en la BD local por pid.</li>
     * <li>Si NO existe, llama a la API de CJ, lo persiste y lo devuelve desde la
     * BD.</li>
     * <li>Si YA existe, lo devuelve directamente desde la BD.</li>
     * </ol>
     *
     * @param pid    CJ product ID
     * @param locale Código de idioma para filtrar traducciones
     * @return Detalle del producto con traducciones, variantes e inventarios
     */
    ProductDetail getOrFetchFromCj(String pid, String locale);

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    /**
     * Lista todas las variantes de forma paginada, con búsqueda opcional.
     */
    Page<ProductDetailVariant> findAllVariantsPaged(int page, int size, String search,
            String status, String sortBy, boolean ascending);

    /**
     * Lista todas las variantes de un producto.
     */
    List<ProductDetailVariant> findVariantsByPid(String pid);

    /**
     * Obtiene una variante por su vid.
     */
    ProductDetailVariant findVariantByVid(String vid);

    /**
     * Crea una nueva variante manualmente.
     */
    ProductDetailVariant createVariant(ProductDetailVariant variant);

    /**
     * Actualiza una variante existente.
     */
    ProductDetailVariant updateVariant(String vid, ProductDetailVariant variant);

    /**
     * Elimina una variante por su vid.
     */
    void deleteVariant(String vid);

    /**
     * Publica o despublica una variante (toggle DRAFT/PUBLISHED).
     */
    void publishVariant(String vid);

    /**
     * Elimina múltiples variantes por sus vids.
     */
    void deleteVariants(List<String> vids);

    /**
     * Cambia el estado de múltiples variantes a DRAFT o PUBLISHED.
     */
    void bulkUpdateVariantStatus(List<String> vids, String status);

    /**
     * Carga masiva de variantes. Crea cada variante individualmente,
     * acumulando errores por fila sin abortar el lote completo.
     */
    BulkImportResult bulkCreateVariants(List<ProductDetailVariant> variants);
}
