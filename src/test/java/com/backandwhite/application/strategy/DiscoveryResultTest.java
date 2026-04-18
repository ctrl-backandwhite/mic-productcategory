package com.backandwhite.application.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DiscoveryResultTest {

    @Test
    void builder_setsAllFields() {
        DiscoveryResult r = DiscoveryResult.builder().newPidsDiscovered(5).totalPidsProcessed(10).pagesScanned(2)
                .lastProcessedItem("x").completed(true).errorMessage("err").build();
        assertThat(r.getNewPidsDiscovered()).isEqualTo(5);
        assertThat(r.getTotalPidsProcessed()).isEqualTo(10);
        assertThat(r.getPagesScanned()).isEqualTo(2);
        assertThat(r.getLastProcessedItem()).isEqualTo("x");
        assertThat(r.isCompleted()).isTrue();
        assertThat(r.getErrorMessage()).isEqualTo("err");
    }

    @Test
    void noArgsAndAllArgsConstructor_work() {
        DiscoveryResult empty = new DiscoveryResult();
        empty.setNewPidsDiscovered(1);
        assertThat(empty.getNewPidsDiscovered()).isEqualTo(1);
        DiscoveryResult full = new DiscoveryResult(1, 2, 3, "i", true, null);
        assertThat(full.getPagesScanned()).isEqualTo(3);
    }
}
