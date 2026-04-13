package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.CjSyncResult;

public interface CjInventorySyncUseCase {

    /**
     * Syncs inventory for all products that are stale (>4h since last sync).
     *
     * @param force if true, re-sync even products synced recently
     */
    CjSyncResult syncAll(boolean force);

    /**
     * Syncs inventory for a single product by pid.
     */
    CjSyncResult syncByPid(String pid);
}
