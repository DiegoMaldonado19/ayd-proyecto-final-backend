package com.ayd.parkcontrol.infrastructure.security;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security evaluator for plate change request ownership validation.
 * Used in @PreAuthorize expressions to ensure clients can only access their own
 * plate change requests.
 */
@Component("plateChangeSecurity")
@RequiredArgsConstructor
public class PlateChangeSecurityEvaluator {

    private final JpaPlateChangeRequestRepository jpaPlateChangeRequestRepository;
    private final JpaSubscriptionRepository jpaSubscriptionRepository;

    /**
     * Verifies if the current authenticated user is the owner of the plate change
     * request.
     *
     * @param requestId ID of the plate change request to check
     * @return true if the authenticated user owns the request, false otherwise
     */
    public boolean isOwner(Long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();

        return jpaPlateChangeRequestRepository.findById(requestId)
                .map(request -> {
                    // Get subscription to check ownership
                    return jpaSubscriptionRepository.findById(request.getSubscriptionId())
                            .map(subscription -> subscription.getUser().getEmail().equals(email))
                            .orElse(false);
                })
                .orElse(false);
    }

    /**
     * Checks if current user is the owner OR has administrator/operator role.
     *
     * @param requestId ID of the plate change request to check
     * @return true if user is owner or has admin/operator role
     */
    public boolean isOwnerOrOperator(Long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check if user has admin or operator role
        boolean isOperator = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Administrador") ||
                        auth.getAuthority().equals("ROLE_Operador Back Office"));

        if (isOperator) {
            return true;
        }

        // Otherwise check ownership
        return isOwner(requestId);
    }
}
