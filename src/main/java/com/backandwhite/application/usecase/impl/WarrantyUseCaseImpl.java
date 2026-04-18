package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.WarrantyUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.repository.WarrantyRepository;
import com.backandwhite.domain.valueobject.WarrantyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class WarrantyUseCaseImpl implements WarrantyUseCase {

    private final WarrantyRepository warrantyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Warranty> findAll(Boolean active, WarrantyType type, int page, int size, String sortBy,
            boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return warrantyRepository.findAll(active, type, PageRequest.of(page, size, sort));
    }

    @Override
    @Transactional(readOnly = true)
    public Warranty findById(String id) {
        return getByIdOrThrow(id);
    }

    @Override
    @Transactional
    public Warranty create(Warranty warranty) {
        warranty.setActive(true);
        Warranty saved = warrantyRepository.save(warranty);
        log.info("Warranty created: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Warranty update(String id, Warranty warranty) {
        Warranty existing = getByIdOrThrow(id);
        warranty.setId(existing.getId());
        warranty.setActive(existing.getActive());
        Warranty updated = warrantyRepository.update(warranty);
        log.info("Warranty updated: {} ({})", updated.getName(), id);
        return updated;
    }

    @Override
    @Transactional
    public void delete(String id) {
        getByIdOrThrow(id);
        warrantyRepository.deleteById(id);
        log.info("Warranty deleted: {}", id);
    }

    @Override
    @Transactional
    public void toggleActive(String id) {
        Warranty existing = getByIdOrThrow(id);
        boolean newActive = !Boolean.TRUE.equals(existing.getActive());
        existing.setActive(newActive);
        warrantyRepository.update(existing);
        log.info("Warranty {} toggled to active={}", id, newActive);
    }

    private Warranty getByIdOrThrow(String id) {
        return warrantyRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Warranty", id));
    }
}
