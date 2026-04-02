package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.AttributeUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AttributeUseCaseImpl implements AttributeUseCase {

    private final AttributeRepository attributeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Attribute> findAll(String name, int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return attributeRepository.findAll(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Attribute findById(String attributeId) {
        return attributeRepository.findById(attributeId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Attribute", attributeId));
    }

    @Override
    @Transactional
    public Attribute create(Attribute attribute) {
        return attributeRepository.save(attribute);
    }

    @Override
    @Transactional
    public Attribute update(String attributeId, Attribute attribute) {
        return attributeRepository.update(attributeId, attribute);
    }

    @Override
    @Transactional
    public void delete(String attributeId) {
        attributeRepository.delete(attributeId);
    }
}
