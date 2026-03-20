package com.backandwhite.infrastructure.client.cj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjAccessTokenRequestDto {

    private String apiKey;
}
