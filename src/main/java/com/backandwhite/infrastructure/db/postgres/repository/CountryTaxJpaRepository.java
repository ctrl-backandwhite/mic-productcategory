package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryTaxJpaRepository extends JpaRepository<CountryTaxEntity, String> {

    List<CountryTaxEntity> findByActiveTrue();

    List<CountryTaxEntity> findByCountryCode(String countryCode);

    List<CountryTaxEntity> findByCountryCodeAndActiveTrue(String countryCode);
}
