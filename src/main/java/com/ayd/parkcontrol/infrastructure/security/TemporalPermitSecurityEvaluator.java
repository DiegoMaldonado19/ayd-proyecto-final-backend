package com.ayd.parkcontrol.infrastructure.security;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security evaluator for temporal permit ownership validation.
 * Used in @PreAuthorize expressions to ensure clients can only access their own
 * temporal permits.
 */
@Component("temporalPermitSecurity")
@RequiredArgsConstructor
public class TemporalPermitSecurityEvaluator {

    private final JpaTemporalPermitRepository jpaTemporalPermitRepository;
    private final JpaSubscriptionRepository jpaSubscriptionRepository;

    /**
     * Verifies if the current authenticated user is the owner of the temporal
     * permit.
     *
     * @param permitId ID of the temporal permit to check
     * @return true if the authenticated user owns the permit, false otherwise
     */
    public boolean isOwner(Long permitId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();

        return jpaTemporalPermitRepository.findById(permitId)
                .map(permit -> {
                    // Get subscription to check ownership
                    return jpaSubscriptionRepository.findById(permit.getSubscriptionId())
                            .map(subscription -> subscription.getUser().getEmail().equals(email))
                            .orElse(false);
                })
                .orElse(false);
    }

    /**
     * Checks if current user is the owner OR has administrator/operator role.
     *
     * @param permitId ID of the temporal permit to check
     * @return true if user is owner or has admin/operator role
     */
    public boolean isOwnerOrOperator(Long permitId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check if user has admin or branch operator role
        boolean isOperator = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Administrador") ||
                        auth.getAuthority().equals("ROLE_Operador Sucursal"));

        if (isOperator) {
            return true;
        }

        // Otherwise check ownership
        return isOwner(permitId);
    }
}
