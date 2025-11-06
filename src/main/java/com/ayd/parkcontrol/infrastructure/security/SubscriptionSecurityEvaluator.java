package com.ayd.parkcontrol.infrastructure.security;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security evaluator for subscription ownership validation.
 * Used in @PreAuthorize expressions to ensure clients can only access their own
 * subscriptions.
 */
@Component("subscriptionSecurity")
@RequiredArgsConstructor
public class SubscriptionSecurityEvaluator {

    private final JpaSubscriptionRepository jpaSubscriptionRepository;
    private final JpaUserRepository jpaUserRepository;

    /**
     * Verifies if the current authenticated user is the owner of the subscription.
     *
     * @param subscriptionId ID of the subscription to check
     * @return true if the authenticated user owns the subscription, false otherwise
     */
    public boolean isOwner(Long subscriptionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get user ID from CustomUserDetails
        Long userId;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUserId();
        } else {
            // Fallback: get user by email
            String email = authentication.getName();
            userId = jpaUserRepository.findByEmail(email)
                    .map(user -> user.getId())
                    .orElse(null);
            
            if (userId == null) {
                return false;
            }
        }

        // Compare userId directly without loading the user entity
        return jpaSubscriptionRepository.findById(subscriptionId)
                .map(subscription -> subscription.getUserId().equals(userId))
                .orElse(false);
    }

    /**
     * Checks if current user is the owner OR has administrator role.
     *
     * @param subscriptionId ID of the subscription to check
     * @return true if user is owner or admin
     */
    public boolean isOwnerOrAdmin(Long subscriptionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check if user has admin role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_Administrador"));

        if (isAdmin) {
            return true;
        }

        // Otherwise check ownership
        return isOwner(subscriptionId);
    }
}
