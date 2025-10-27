package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFleetUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private GetFleetUseCase getFleetUseCase;

    private FleetCompanyEntity fleetCompany;
    private FleetResponse fleetResponse;

    @BeforeEach
    void setUp() {
        fleetCompany = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Fleet Company")
                .taxId("12345678-9")
                .corporateEmail("test@fleet.com")
                .phone("555-1234")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .build();

        fleetResponse = FleetResponse.builder()
                .id(1L)
                .name("Test Fleet Company")
                .taxId("12345678-9")
                .corporateEmail("test@fleet.com")
                .phone("555-1234")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .activeVehiclesCount(5L)
                .build();
    }

    @Test
    void shouldGetFleetSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(fleetCompany));
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(5L);
        when(fleetDtoMapper.toResponse(fleetCompany, 5L)).thenReturn(fleetResponse);

        // When
        FleetResponse response = getFleetUseCase.execute(companyId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Fleet Company");
        assertThat(response.getTaxId()).isEqualTo("12345678-9");
        assertThat(response.getCorporateEmail()).isEqualTo("test@fleet.com");
        assertThat(response.getPhone()).isEqualTo("555-1234");
        assertThat(response.getCorporateDiscountPercentage()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(response.getPlateLimit()).isEqualTo(20);
        assertThat(response.getBillingPeriod()).isEqualTo("MONTHLY");
        assertThat(response.getMonthsUnpaid()).isEqualTo(0);
        assertThat(response.getIsActive()).isTrue();

        verify(fleetCompanyRepository).findById(companyId);
        verify(fleetCompanyRepository).countActiveVehiclesByCompanyId(companyId);
        verify(fleetDtoMapper).toResponse(fleetCompany, 5L);
    }

    @Test
    void shouldThrowExceptionWhenFleetNotFound() {
        // Given
        Long companyId = 999L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getFleetUseCase.execute(companyId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empresa flotillera no encontrada con ID: 999");

        verify(fleetCompanyRepository).findById(companyId);
    }

    @Test
    void shouldHandleInactiveFleet() {
        // Given
        Long companyId = 1L;
        fleetCompany.setIsActive(false);
        fleetResponse.setIsActive(false);

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(fleetCompany));
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(5L);
        when(fleetDtoMapper.toResponse(fleetCompany, 5L)).thenReturn(fleetResponse);

        // When
        FleetResponse response = getFleetUseCase.execute(companyId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getIsActive()).isFalse();

        verify(fleetCompanyRepository).findById(companyId);
        verify(fleetCompanyRepository).countActiveVehiclesByCompanyId(companyId);
        verify(fleetDtoMapper).toResponse(fleetCompany, 5L);
    }
}
