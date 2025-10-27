package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.exception.MerchantNotAffiliatedException;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para MerchantBenefitValidator.
 */
@ExtendWith(MockitoExtension.class)
class MerchantBenefitValidatorTest {

    @Mock
    private JpaBranchBusinessRepository branchBusinessRepository;

    @InjectMocks
    private MerchantBenefitValidator validator;

    @Test
    void validateAffiliation_whenAffiliated_shouldNotThrowException() {
        // Arrange
        Long businessId = 1L;
        Long branchId = 2L;
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId))
                .thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> validator.validateAffiliation(businessId, branchId));
        verify(branchBusinessRepository).existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId);
    }

    @Test
    void validateAffiliation_whenNotAffiliated_shouldThrowException() {
        // Arrange
        Long businessId = 1L;
        Long branchId = 2L;
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId))
                .thenReturn(false);

        // Act & Assert
        assertThrows(MerchantNotAffiliatedException.class, () -> {
            validator.validateAffiliation(businessId, branchId);
        });
        verify(branchBusinessRepository).existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId);
    }

    @Test
    void isAffiliated_whenAffiliated_shouldReturnTrue() {
        // Arrange
        Long businessId = 1L;
        Long branchId = 2L;
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId))
                .thenReturn(true);

        // Act
        boolean result = validator.isAffiliated(businessId, branchId);

        // Assert
        assertTrue(result);
        verify(branchBusinessRepository).existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId);
    }

    @Test
    void isAffiliated_whenNotAffiliated_shouldReturnFalse() {
        // Arrange
        Long businessId = 1L;
        Long branchId = 2L;
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId))
                .thenReturn(false);

        // Act
        boolean result = validator.isAffiliated(businessId, branchId);

        // Assert
        assertFalse(result);
        verify(branchBusinessRepository).existsByBusinessIdAndBranchIdAndIsActiveTrue(businessId, branchId);
    }
}
