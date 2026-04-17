package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.ProductSyncResult;
import java.util.List;

public interface ProductSyncUseCase {

    ProductSyncResult syncFromCjDropshipping(boolean forceOverwrite);

    ProductSyncResult syncPageFromCjDropshipping(int page, int size, boolean forceOverwrite, List<String> categoryIds);

    /**
     * Discovers NEW products from CJ by iterating L3 categories already synced.
     * Processes ONE category at the given offset, fetches products from CJ listV2,
     * filters out those already in the DB, and imports new ones.
     *
     * @param categoryOffset
     *            0-based index into the sorted L3 category list
     * @return sync result with hasMore=true if more categories remain
     */
    ProductSyncResult discoverNewProductsByCategory(int categoryOffset);
}
