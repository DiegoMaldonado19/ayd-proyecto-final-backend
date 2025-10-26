package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SettlementTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSettlementTicketRepository extends JpaRepository<SettlementTicketEntity, Long> {

    List<SettlementTicketEntity> findBySettlementId(Long settlementId);

    List<SettlementTicketEntity> findByTicketId(Long ticketId);

    boolean existsBySettlementIdAndTicketId(Long settlementId, Long ticketId);
}
