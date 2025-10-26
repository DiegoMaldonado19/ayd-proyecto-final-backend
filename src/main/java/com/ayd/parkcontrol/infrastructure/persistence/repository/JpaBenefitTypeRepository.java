package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BenefitTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaBenefitTypeRepository extends JpaRepository<BenefitTypeEntity, Integer> {

    Optional<BenefitTypeEntity> findByCode(String code);
}
