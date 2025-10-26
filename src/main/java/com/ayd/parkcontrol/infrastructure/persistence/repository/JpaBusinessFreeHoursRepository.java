package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessFreeHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JpaBusinessFreeHoursRepository extends JpaRepository<BusinessFreeHoursEntity, Long> {

    /**
     * Buscar horas gratis por ticket
     */
    List<BusinessFreeHoursEntity> findByTicketId(Long ticketId);

    /**
     * Calcular total de horas gratis otorgadas a un ticket
     */
    @Query("SELECT COALESCE(SUM(bfh.grantedHours), 0) FROM BusinessFreeHoursEntity bfh WHERE bfh.ticketId = :ticketId")
    BigDecimal sumGrantedHoursByTicketId(@Param("ticketId") Long ticketId);
}
