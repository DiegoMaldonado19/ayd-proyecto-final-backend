package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para comercios afiliados
 */
@Repository
public interface JpaAffiliatedBusinessRepository extends JpaRepository<AffiliatedBusinessEntity, Long> {

    /**
     * Buscar comercio afiliado por tax ID
     */
    Optional<AffiliatedBusinessEntity> findByTaxId(String taxId);

    /**
     * Buscar comercio afiliado activo por ID
     */
    Optional<AffiliatedBusinessEntity> findByIdAndIsActiveTrue(Long id);
}
