package com.backandwhite.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSyncResult {

    private int created;
    private int updated;
    private int total;
    private int page;
    private boolean hasMore;
}
