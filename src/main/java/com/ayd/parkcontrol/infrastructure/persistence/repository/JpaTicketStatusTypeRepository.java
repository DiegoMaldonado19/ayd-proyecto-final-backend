package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTicketStatusTypeRepository extends JpaRepository<TicketStatusTypeEntity, Integer> {

    Optional<TicketStatusTypeEntity> findByCode(String code);
}
