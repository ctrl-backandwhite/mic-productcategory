package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryTaxInfraMapper {

    CountryTax toDomain(CountryTaxEntity entity);

    List<CountryTax> toDomainList(List<CountryTaxEntity> entities);

    CountryTaxEntity toEntity(CountryTax domain);
}
