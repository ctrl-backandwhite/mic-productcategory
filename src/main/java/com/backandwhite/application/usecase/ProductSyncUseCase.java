package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.ProductSyncResult;

public interface ProductSyncUseCase {

    ProductSyncResult syncFromCjDropshipping();

    ProductSyncResult syncPageFromCjDropshipping(int page, int size);
}
