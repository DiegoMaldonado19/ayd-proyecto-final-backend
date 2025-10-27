package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.exception.LostTicketTimeframeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validador de incidentes de tickets extraviados.
 * 
 * Regla de Negocio: Solo se permiten reportes de tickets extraviados dentro de
 * las 24 horas posteriores al ingreso del vehículo.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class IncidentValidator {

    private static final int MAX_HOURS_LOST_TICKET = 24;

    /**
     * Valida que un reporte de ticket extraviado esté dentro del límite de 24
     * horas.
     * 
     * @param entryTime fecha/hora de ingreso del vehículo
     * @throws LostTicketTimeframeException si excede el límite de 24 horas
     */
    public void validateLostTicketTimeframe(LocalDateTime entryTime) {
        LocalDateTime now = LocalDateTime.now();
        long hoursSinceEntry = ChronoUnit.HOURS.between(entryTime, now);

        log.debug("Validando timeframe de ticket extraviado. Horas desde ingreso: {}", hoursSinceEntry);

        if (hoursSinceEntry > MAX_HOURS_LOST_TICKET) {
            log.warn("Ticket extraviado excede límite. Ingreso: {}, Horas transcurridas: {}",
                    entryTime, hoursSinceEntry);
            throw new LostTicketTimeframeException(hoursSinceEntry);
        }

        log.debug("Validación de timeframe exitosa. Dentro del límite de 24h");
    }

    /**
     * Verifica si un reporte de ticket extraviado está dentro del plazo válido (sin
     * lanzar excepción).
     * 
     * @param entryTime fecha/hora de ingreso del vehículo
     * @return true si está dentro del plazo, false si excede las 24 horas
     */
    public boolean isWithinValidTimeframe(LocalDateTime entryTime) {
        long hoursSinceEntry = ChronoUnit.HOURS.between(entryTime, LocalDateTime.now());
        return hoursSinceEntry <= MAX_HOURS_LOST_TICKET;
    }
}
