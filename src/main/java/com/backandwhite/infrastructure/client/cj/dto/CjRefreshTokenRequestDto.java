package com.backandwhite.infrastructure.client.cj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CjRefreshTokenRequestDto {

    private String refreshToken;
}
