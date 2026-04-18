package com.backandwhite.application.port.out;

import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import java.util.List;

/**
 * Port interface for dropshipping supplier interactions (CJ Dropshipping).
 */
public interface DropshippingPort {

    /**
     * Fetches categories from the dropshipping supplier.
     */
    List<CjCategoryFirstLevelDto> getCategories();

    /**
     * Fetches product detail by pid from the dropshipping supplier.
     */
    CjProductDetailDto getProductDetail(String pid);

    /**
     * Fetches a page of products from the dropshipping supplier.
     */
    CjProductListPageDto getProductList(int page, int size);

    /**
     * Fetches inventory per variant for a given product pid. Uses
     * /product/stock/getInventoryByPid endpoint.
     */
    List<CjInventoryByPidItemDto> getInventoryByPid(String pid);

    /**
     * Fetches a page of product reviews (comments) from CJ. Uses
     * /product/productComments endpoint.
     *
     * @param pid
     *            product id
     * @param score
     *            filter by star rating (0 = all)
     * @param page
     *            1-based page number
     * @param size
     *            page size
     */
    CjProductCommentsPageDto getProductComments(String pid, int score, int page, int size);

    /**
     * Fetches a filtered page of products from CJ using listV2 endpoint. All filter
     * parameters are optional — pass null to omit.
     *
     * @param page
     *            1-based page number (max 1000)
     * @param size
     *            page size (max 100)
     * @param categoryId
     *            L3 category ID filter
     * @param keyword
     *            search keyword filter
     * @param timeStart
     *            filter products created after this timestamp (millis)
     * @param timeEnd
     *            filter products created before this timestamp (millis)
     * @param orderBy
     *            0=bestMatch, 1=listedCount, 2=sellPrice, 3=createTime, 4=inventory
     * @param sort
     *            "desc" or "asc"
     */
    @SuppressWarnings("java:S107")
    CjProductListPageDto getProductListFiltered(int page, int size, String categoryId, String keyword, Long timeStart,
            Long timeEnd, Integer orderBy, String sort);
}
