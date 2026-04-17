package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscoveredPidRepository {

    boolean existsByPid(String pid);

    DiscoveredPid save(DiscoveredPid discoveredPid);

    List<DiscoveredPid> saveAll(List<DiscoveredPid> pids);

    List<DiscoveredPid> findByStatus(DiscoveryStatus status, int limit);

    Page<DiscoveredPid> findByStatusPaged(DiscoveryStatus status, Pageable pageable);

    Page<DiscoveredPid> findAll(DiscoveryStatus status, DiscoveryStrategy strategy, Pageable pageable);

    long countByStatus(DiscoveryStatus status);

    long countByStrategy(DiscoveryStrategy strategy);

    long countAll();

    void updateStatus(String id, DiscoveryStatus status);

    void markSynced(String id);

    void markFailed(String id, String error);

    int bulkUpdateStatus(DiscoveryStatus fromStatus, DiscoveryStatus toStatus, int limit);
}
