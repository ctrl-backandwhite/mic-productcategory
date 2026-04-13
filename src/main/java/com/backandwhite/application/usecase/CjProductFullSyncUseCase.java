package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.CjSyncResult;

public interface CjProductFullSyncUseCase {

    /**
     * Syncs full product detail (name, description, images, variants, translations)
     * for all stale products.
     *
     * @param force if true, re-sync even products synced today
     */
    CjSyncResult syncAll(boolean force);

    /**
     * Syncs full product detail for a single product by pid.
     */
    CjSyncResult syncByPid(String pid);
}
