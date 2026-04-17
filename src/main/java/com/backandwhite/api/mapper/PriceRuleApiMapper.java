package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.PriceRuleDtoIn;
import com.backandwhite.api.dto.out.PriceRuleDtoOut;
import com.backandwhite.domain.model.PriceRule;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceRuleApiMapper {

    PriceRuleDtoOut toDto(PriceRule domain);

    List<PriceRuleDtoOut> toDtoList(List<PriceRule> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PriceRule toDomain(PriceRuleDtoIn dto);
}
