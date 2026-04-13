package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single review item from CJ /product/productComments endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjReviewItemDto {

    private String id;
    private String pid;
    private String vid;
    private String sku;
    private String orderNo;
    private Integer score;
    private String createDate;
    private String reviewContent;
    private String reviewImages;
    private String countryCode;
    private String reviewerName;
    private Boolean verifiedPurchase;
}
