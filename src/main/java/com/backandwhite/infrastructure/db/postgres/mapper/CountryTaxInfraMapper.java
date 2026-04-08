package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryTaxInfraMapper {

    CountryTax toDomain(CountryTaxEntity entity);

    List<CountryTax> toDomainList(List<CountryTaxEntity> entities);

    CountryTaxEntity toEntity(CountryTax domain);
}
