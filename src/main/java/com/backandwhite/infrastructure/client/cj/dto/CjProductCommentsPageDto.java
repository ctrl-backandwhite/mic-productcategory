package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response from CJ /product/productComments endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductCommentsPageDto {

    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    private List<CjReviewItemDto> list = new ArrayList<>();
}
