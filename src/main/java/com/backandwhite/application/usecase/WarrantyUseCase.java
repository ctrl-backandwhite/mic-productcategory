package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.valueobject.WarrantyType;
import org.springframework.data.domain.Page;

public interface WarrantyUseCase {

    Page<Warranty> findAll(Boolean active, WarrantyType type, int page, int size, String sortBy, boolean ascending);

    Warranty findById(String id);

    Warranty create(Warranty warranty);

    Warranty update(String id, Warranty warranty);

    void delete(String id);

    void toggleActive(String id);
}
