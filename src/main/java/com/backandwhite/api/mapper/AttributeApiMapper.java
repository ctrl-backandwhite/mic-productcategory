package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.AttributeDtoIn;
import com.backandwhite.api.dto.in.AttributeValueDtoIn;
import com.backandwhite.api.dto.out.AttributeDtoOut;
import com.backandwhite.api.dto.out.AttributeValueDtoOut;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeApiMapper {

    AttributeDtoOut toDto(Attribute attribute);

    List<AttributeDtoOut> toDtoList(List<Attribute> attributes);

    AttributeValueDtoOut toValueDto(AttributeValue value);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedInProducts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attribute toDomain(AttributeDtoIn dto);

    @Mapping(target = "attributeId", ignore = true)
    @Mapping(target = "position", ignore = true)
    AttributeValue toValueDomain(AttributeValueDtoIn dto);
}
