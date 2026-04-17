package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Brand;
import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandInfraMapper {

    @Mapping(target = "productCount", ignore = true)
    Brand toDomain(BrandEntity entity);

    List<Brand> toDomainList(List<BrandEntity> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BrandEntity toEntity(Brand domain);
}
