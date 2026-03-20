package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a paged response from CJ product/listV2 endpoint.
 * The "data" field of CjApiResponseDto wraps this object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductListPageDto {

    private Integer pageNum;
    private Integer pageSize;
    private Integer total;
    private List<CjProductDetailDto> list;
}
