package com.ayd.parkcontrol.domain.exception;

/**
 * Excepción lanzada cuando un comercio intenta acceder a beneficios
 * de una sucursal a la que no está afiliado.
 * 
 * Regla de Negocio: Solo comercios afiliados activamente a una sucursal
 * pueden otorgar beneficios (horas gratis, tarifas especiales) en esa
 * ubicación.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
public class MerchantNotAffiliatedException extends BusinessRuleException {

    public MerchantNotAffiliatedException(String message) {
        super(message);
    }

    public MerchantNotAffiliatedException(Long merchantId, Long branchId) {
        super(String.format(
                "Comercio con ID %d no está afiliado a la sucursal con ID %d",
                merchantId,
                branchId));
    }

    public MerchantNotAffiliatedException(String merchantName, String branchName) {
        super(String.format(
                "El comercio '%s' no está afiliado a la sucursal '%s'",
                merchantName,
                branchName));
    }
}
