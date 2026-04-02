package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.WarrantyDtoIn;
import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.domain.model.Warranty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WarrantyApiMapper {

    WarrantyDtoOut toDto(Warranty warranty);

    List<WarrantyDtoOut> toDtoList(List<Warranty> warranties);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "productsCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Warranty toDomain(WarrantyDtoIn dto);
}
