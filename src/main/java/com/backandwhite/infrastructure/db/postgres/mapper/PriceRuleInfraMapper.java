package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.infrastructure.db.postgres.entity.PriceRuleEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriceRuleInfraMapper {

    PriceRule toDomain(PriceRuleEntity entity);

    List<PriceRule> toDomainList(List<PriceRuleEntity> entities);

    PriceRuleEntity toEntity(PriceRule domain);
}
