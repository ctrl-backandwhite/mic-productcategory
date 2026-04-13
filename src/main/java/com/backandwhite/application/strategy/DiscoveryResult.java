package com.backandwhite.application.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryResult {
    private int newPidsDiscovered;
    private int totalPidsProcessed;
    private int pagesScanned;
    private String lastProcessedItem;
    private boolean completed;
    private String errorMessage;
}
