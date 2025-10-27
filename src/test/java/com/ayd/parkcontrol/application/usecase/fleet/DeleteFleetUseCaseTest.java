package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFleetUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @InjectMocks
    private DeleteFleetUseCase deleteFleetUseCase;

    private FleetCompanyEntity fleetCompany;

    @BeforeEach
    void setUp() {
        fleetCompany = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Fleet Company")
                .taxId("12345678-9")
                .corporateEmail("test@fleet.com")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .build();
    }

    @Test
    void shouldDeleteFleetSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.existsById(companyId)).thenReturn(true);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(0L);
        doNothing().when(fleetCompanyRepository).deleteById(companyId);

        // When
        deleteFleetUseCase.execute(companyId);

        // Then
        verify(fleetCompanyRepository).existsById(companyId);
        verify(fleetCompanyRepository).countActiveVehiclesByCompanyId(companyId);
        verify(fleetCompanyRepository).deleteById(companyId);
    }

    @Test
    void shouldThrowExceptionWhenFleetNotFound() {
        // Given
        Long companyId = 999L;
        when(fleetCompanyRepository.existsById(companyId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> deleteFleetUseCase.execute(companyId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empresa flotillera no encontrada con ID: 999");

        verify(fleetCompanyRepository).existsById(companyId);
        verify(fleetCompanyRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteInactiveFleet() {
        // Given
        Long companyId = 1L;
        fleetCompany.setIsActive(false);
        when(fleetCompanyRepository.existsById(companyId)).thenReturn(true);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(0L);
        doNothing().when(fleetCompanyRepository).deleteById(companyId);

        // When
        deleteFleetUseCase.execute(companyId);

        // Then
        verify(fleetCompanyRepository).existsById(companyId);
        verify(fleetCompanyRepository).countActiveVehiclesByCompanyId(companyId);
        verify(fleetCompanyRepository).deleteById(companyId);
    }
}
