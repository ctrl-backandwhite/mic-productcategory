package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Warranty;
import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarrantyInfraMapper {

    @Mapping(target = "productsCount", ignore = true)
    Warranty toDomain(WarrantyEntity entity);

    List<Warranty> toDomainList(List<WarrantyEntity> entities);

    WarrantyEntity toEntity(Warranty domain);
}
