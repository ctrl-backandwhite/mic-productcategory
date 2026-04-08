package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.domain.valueobject.PriceRuleScope;
import com.backandwhite.infrastructure.db.postgres.entity.PriceRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRuleJpaRepository extends JpaRepository<PriceRuleEntity, String> {

    List<PriceRuleEntity> findByActiveTrue();

    Optional<PriceRuleEntity> findByScopeAndScopeId(PriceRuleScope scope, String scopeId);

    List<PriceRuleEntity> findByScopeAndActiveTrueOrderByPriorityDesc(PriceRuleScope scope);
}
