package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjApiResponseDto<T> {

    private Integer code;
    private Boolean result;
    private String message;
    private T data;
    private String requestId;
    private Boolean success;
}
