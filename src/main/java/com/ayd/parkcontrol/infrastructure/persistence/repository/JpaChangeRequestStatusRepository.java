package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaChangeRequestStatusRepository extends JpaRepository<ChangeRequestStatusEntity, Integer> {

    Optional<ChangeRequestStatusEntity> findByCode(String code);
}
