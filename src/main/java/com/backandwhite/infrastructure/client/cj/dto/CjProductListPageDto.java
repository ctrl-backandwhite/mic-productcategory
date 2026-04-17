package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a paged response from CJ product/listV2 endpoint. The "data" field
 * of CjApiResponseDto wraps this object.
 *
 * CJ actual response: { pageSize, pageNumber, totalRecords, totalPages,
 * content: [ { productList: [...], ... } ] }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductListPageDto {

    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalRecords;
    private Integer totalPages;
    private List<CjProductListV2ContentDto> content;

    /**
     * Flattens all products from nested content[].productList[] into a single list.
     */
    public List<CjProductListV2ItemDto> getAllProducts() {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        List<CjProductListV2ItemDto> all = new ArrayList<>();
        for (CjProductListV2ContentDto entry : content) {
            if (entry.getProductList() != null) {
                all.addAll(entry.getProductList());
            }
        }
        return all;
    }
}
