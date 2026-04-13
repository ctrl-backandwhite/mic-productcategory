package com.backandwhite.api.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryResultDtoOut {
    private int newPidsDiscovered;
    private int totalPidsProcessed;
    private int pagesScanned;
    private boolean completed;
    private String errorMessage;
}
