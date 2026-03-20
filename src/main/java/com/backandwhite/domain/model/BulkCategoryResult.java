package com.backandwhite.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCategoryResult {

    private int created;
    private int skipped;
    private int totalRows;
}
