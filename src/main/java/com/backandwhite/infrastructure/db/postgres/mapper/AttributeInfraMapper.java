package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeEntity;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeValueEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttributeInfraMapper {

    @Mapping(target = "usedInProducts", ignore = true)
    Attribute toDomain(AttributeEntity entity);

    List<Attribute> toDomainList(List<AttributeEntity> entities);

    @Mapping(target = "attributeId", source = "attributeId")
    AttributeValue toValueDomain(AttributeValueEntity entity);

    @Mapping(target = "values", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AttributeEntity toEntity(Attribute domain);

    @Mapping(target = "attribute", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AttributeValueEntity toValueEntity(AttributeValue value);

    default AttributeEntity toEntityWithValues(Attribute domain) {
        AttributeEntity entity = toEntity(domain);

        if (domain.getValues() != null) {
            for (AttributeValue v : domain.getValues()) {
                AttributeValueEntity ve = toValueEntity(v);
                ve.setAttribute(entity);
                entity.getValues().add(ve);
            }
        }

        return entity;
    }
}
