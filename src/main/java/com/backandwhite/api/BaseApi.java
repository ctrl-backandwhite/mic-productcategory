package com.backandwhite.api;

import com.backandwhite.api.dto.OperationResponseDtoOut;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface BaseApi<I, O, K> {

// ---------------- CREATE ----------------
    @Operation(
    summary = "Create record",
    description = "Creation of a new record.")
    default ResponseEntity<O> create(@Valid @RequestBody I dto) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Create multiple records",
    description = "Create multiple records at once.")
    default ResponseEntity<OperationResponseDtoOut> createAll(@Valid @RequestBody List<I> dtos) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    // ---------------- UPDATE ----------------
    @Operation(
    summary = "Update record",
    description = "Update a record by ID.")
    default ResponseEntity<O> update(@Valid @RequestBody I dto, @PathVariable K id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Update multiple records",
    description = "Update multiple records at once.")
    default ResponseEntity<OperationResponseDtoOut> updateAll(@Valid @RequestBody List<I> dtos) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    // ---------------- DELETE ----------------
    @Operation(
    summary = "Delete record",
    description = "Delete a record by ID.")
    default ResponseEntity<Void> delete(@PathVariable K id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Delete multiple records",
    description = "Delete multiple records by IDs.")
    default ResponseEntity<Void> deleteAll(@RequestBody List<K> ids) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    // ---------------- READ ----------------
    @Operation(
    summary = "Get record by ID",
    description = "Retrieve a record by its ID.")
    default ResponseEntity<O> getById(@PathVariable K id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Get multiple records by IDs",
    description = "Retrieve multiple records by a list of IDs.")
    default ResponseEntity<List<O>> getByIds(@RequestParam List<K> ids) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Get all records",
    description = "Retrieve all records.")
    default ResponseEntity<List<O>> findAll() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Get paginated and sorted records",
    description = "Retrieve records with pagination and sorting.")
    default ResponseEntity<List<O>> findAllPagedAndSorted(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "true") boolean ascending) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
    summary = "Get filtered records",
    description = "Retrieve records filtered by parameters.")
    default ResponseEntity<List<O>> findAllFiltered(@RequestParam Map<String, Object> filters) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
