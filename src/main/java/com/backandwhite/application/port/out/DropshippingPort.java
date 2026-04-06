package com.backandwhite.application.port.out;

import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
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
}
