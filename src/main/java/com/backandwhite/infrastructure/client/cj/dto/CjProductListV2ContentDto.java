package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents one element of the "content" array inside CJ's listV2 response.
 * Each content entry groups a productList with optional related categories and
 * keyword info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductListV2ContentDto {

    private List<CjProductListV2ItemDto> productList;
    private String keyWord;
    private String keyWordOld;
}
