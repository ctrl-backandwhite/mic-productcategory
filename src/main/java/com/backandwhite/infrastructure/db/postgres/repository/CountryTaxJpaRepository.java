package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryTaxJpaRepository extends JpaRepository<CountryTaxEntity, String> {

    List<CountryTaxEntity> findByActiveTrue();

    List<CountryTaxEntity> findByCountryCode(String countryCode);

    List<CountryTaxEntity> findByCountryCodeAndActiveTrue(String countryCode);
}
