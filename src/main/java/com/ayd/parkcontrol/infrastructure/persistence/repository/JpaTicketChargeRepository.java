package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTicketChargeRepository extends JpaRepository<TicketChargeEntity, Long> {

    /**
     * Buscar cargo por ticket ID
     */
    Optional<TicketChargeEntity> findByTicketId(Long ticketId);
}
