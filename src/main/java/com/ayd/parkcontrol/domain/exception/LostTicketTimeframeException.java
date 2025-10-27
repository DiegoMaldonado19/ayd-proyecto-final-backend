package com.ayd.parkcontrol.domain.exception;

/**
 * Excepción lanzada cuando un reporte de ticket extraviado excede el plazo
 * permitido.
 * 
 * Regla de Negocio: Solo se permiten reportes de tickets extraviados dentro de
 * las 24 horas posteriores al ingreso, para prevenir fraude.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
public class LostTicketTimeframeException extends BusinessRuleException {

    public LostTicketTimeframeException(String message) {
        super(message);
    }

    public LostTicketTimeframeException(long hoursSinceEntry) {
        super(String.format(
                "Ticket extraviado excede el límite de 24 horas. Ingreso hace %d horas.",
                hoursSinceEntry));
    }
}
