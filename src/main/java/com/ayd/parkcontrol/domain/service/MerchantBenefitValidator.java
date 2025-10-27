package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.exception.MerchantNotAffiliatedException;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de validación para afiliaciones de comercios con sucursales.
 * 
 * Regla de Negocio: Un comercio solo puede otorgar beneficios (horas gratis,
 * tarifas especiales) en sucursales a las que está afiliado activamente.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantBenefitValidator {

    private final JpaBranchBusinessRepository branchBusinessRepository;

    /**
     * Valida que un comercio esté afiliado activamente a una sucursal.
     * 
     * @param businessId ID del comercio afiliado
     * @param branchId   ID de la sucursal
     * @throws MerchantNotAffiliatedException si el comercio no está afiliado a la
     *                                        sucursal
     */
    public void validateAffiliation(Long businessId, Long branchId) {
        log.debug("Validando afiliación del comercio {} con sucursal {}", businessId, branchId);

        boolean isAffiliated = branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(
                businessId,
                branchId);

        if (!isAffiliated) {
            log.warn("Comercio {} no afiliado a sucursal {}", businessId, branchId);
            throw new MerchantNotAffiliatedException(businessId, branchId);
        }

        log.debug("Afiliación validada exitosamente para comercio {} y sucursal {}", businessId, branchId);
    }

    /**
     * Verifica si un comercio está afiliado a una sucursal (sin lanzar excepción).
     * 
     * @param businessId ID del comercio afiliado
     * @param branchId   ID de la sucursal
     * @return true si está afiliado activamente, false en caso contrario
     */
    public boolean isAffiliated(Long businessId, Long branchId) {
        return branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(
                businessId,
                branchId);
    }
}
