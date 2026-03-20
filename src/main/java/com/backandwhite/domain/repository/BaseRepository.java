package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.OperationResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface BaseRepository<I, O, K> {

    default O save(I model) {
        return null;
    }

    default OperationResponse saveAll(List<I> models) {
        return null;
    }

    default List<O> findAll() {
        return Collections.emptyList();
    }

    default O update(I model) {
        return null;
    }

    default OperationResponse updateAll(List<I> models) {
        return null;
    }

    default void delete(K id) { }

    default void deleteAll(List<K> ids) { }

    default O getById(K id) {
        return null;
    }

    default List<O> getByIds(List<K> ids) {
        return Collections.emptyList();
    }

    default List<O> findAllPagedAndSorted(int page, int size, String sortBy, boolean ascending) {
        return Collections.emptyList();
    }

    default List<O> findAllFiltered(Map<String, Object> filters) {
        return Collections.emptyList();
    }
}
