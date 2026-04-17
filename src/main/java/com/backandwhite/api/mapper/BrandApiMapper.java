package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.BrandDtoIn;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.domain.model.Brand;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandApiMapper {

    BrandDtoOut toDto(Brand brand);

    List<BrandDtoOut> toDtoList(List<Brand> brands);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Brand toDomain(BrandDtoIn dto);
}
