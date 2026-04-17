package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.valueobject.WarrantyType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarrantyRepository {

    Page<Warranty> findAll(Boolean active, WarrantyType type, Pageable pageable);

    Optional<Warranty> findById(String id);

    Warranty save(Warranty warranty);

    Warranty update(Warranty warranty);

    void deleteById(String id);

    long countProductsByWarrantyId(String warrantyId);
}
