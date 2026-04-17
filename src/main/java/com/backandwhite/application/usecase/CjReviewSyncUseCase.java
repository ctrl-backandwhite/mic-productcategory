package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.CjSyncResult;

public interface CjReviewSyncUseCase {

    /**
     * Syncs reviews for all products that haven't been synced today.
     *
     * @param force
     *            if true, re-sync even products synced today
     */
    CjSyncResult syncAll(boolean force);

    /**
     * Syncs reviews for a single product by pid.
     */
    CjSyncResult syncByPid(String pid);
}
