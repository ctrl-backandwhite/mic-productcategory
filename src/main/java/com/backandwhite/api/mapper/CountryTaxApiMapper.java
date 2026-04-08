package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.CountryTaxDtoIn;
import com.backandwhite.api.dto.out.CountryTaxDtoOut;
import com.backandwhite.domain.model.CountryTax;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryTaxApiMapper {

    @Mapping(target = "country", source = "countryCode")
    @Mapping(target = "appliesToCategories", source = "appliesTo", qualifiedByName = "stringToList")
    @Mapping(target = "type", expression = "java(domain.getType().name())")
    CountryTaxDtoOut toDto(CountryTax domain);

    List<CountryTaxDtoOut> toDtoList(List<CountryTax> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "includesShipping", constant = "true")
    @Mapping(target = "countryCode", source = "country")
    @Mapping(target = "appliesTo", source = "appliesToCategories", qualifiedByName = "listToString")
    CountryTax toDomain(CountryTaxDtoIn dto);

    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.isBlank())
            return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Named("listToString")
    default String listToString(List<String> list) {
        if (list == null || list.isEmpty())
            return null;
        return String.join(", ", list);
    }
}
