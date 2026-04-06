package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.repository.InventoryRepository;
import com.backandwhite.infrastructure.db.postgres.repository.InventoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public int decrementStock(String vid, String countryCode, int quantity) {
        return inventoryJpaRepository.decrementStock(vid, countryCode, quantity);
    }

    @Override
    public int incrementStock(String vid, String countryCode, int quantity) {
        return inventoryJpaRepository.incrementStock(vid, countryCode, quantity);
    }

    @Override
    public int getTotalStockByVid(String vid) {
        return inventoryJpaRepository.getTotalStockByVid(vid);
    }
}
