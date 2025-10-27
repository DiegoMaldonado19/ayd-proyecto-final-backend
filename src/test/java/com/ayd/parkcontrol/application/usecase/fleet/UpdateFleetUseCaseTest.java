package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFleetUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private UpdateFleetUseCase updateFleetUseCase;

    private FleetCompanyEntity fleetCompany;
    private UpdateFleetRequest request;
    private FleetResponse fleetResponse;

    @BeforeEach
    void setUp() {
        fleetCompany = FleetCompanyEntity.builder()
                .id(1L)
                .name("Old Company Name")
                .taxId("12345678-9")
                .corporateEmail("old@fleet.com")
                .phone("555-0000")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .build();

        request = UpdateFleetRequest.builder()
                .name("New Company Name")
                .corporateEmail("new@fleet.com")
                .phone("555-9999")
                .plateLimit(30)
                .billingPeriod("QUARTERLY")
                .isActive(true)
                .build();

        fleetResponse = FleetResponse.builder()
                .id(1L)
                .name("New Company Name")
                .taxId("12345678-9")
                .corporateEmail("new@fleet.com")
                .phone("555-9999")
                .plateLimit(30)
                .billingPeriod("QUARTERLY")
                .isActive(true)
                .build();
    }

    @Test
    void shouldUpdateFleetSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(fleetCompany));
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(fleetCompany);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(5L);
        when(fleetDtoMapper.toResponse(any(FleetCompanyEntity.class), anyLong())).thenReturn(fleetResponse);

        // When
        FleetResponse response = updateFleetUseCase.execute(companyId, request);

        // Then
        ArgumentCaptor<FleetCompanyEntity> captor = ArgumentCaptor.forClass(FleetCompanyEntity.class);
        verify(fleetCompanyRepository).save(captor.capture());
        FleetCompanyEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("New Company Name");
        assertThat(savedEntity.getCorporateEmail()).isEqualTo("new@fleet.com");
        assertThat(savedEntity.getPhone()).isEqualTo("555-9999");
        assertThat(savedEntity.getPlateLimit()).isEqualTo(30);
        assertThat(savedEntity.getBillingPeriod()).isEqualTo("QUARTERLY");
        assertThat(savedEntity.getIsActive()).isTrue();

        assertThat(response).isNotNull();
        verify(fleetCompanyRepository).findById(companyId);
        verify(fleetCompanyRepository).save(any(FleetCompanyEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenFleetNotFound() {
        // Given
        Long companyId = 999L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empresa flotillera no encontrada con ID: 999");

        verify(fleetCompanyRepository).findById(companyId);
        verify(fleetCompanyRepository, never()).save(any());
    }

    @Test
    void shouldUpdatePartialFields() {
        // Given
        Long companyId = 1L;
        UpdateFleetRequest partialRequest = UpdateFleetRequest.builder()
                .name("Updated Name Only")
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(fleetCompany));
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(fleetCompany);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(5L);
        when(fleetDtoMapper.toResponse(any(FleetCompanyEntity.class), anyLong())).thenReturn(fleetResponse);

        // When
        updateFleetUseCase.execute(companyId, partialRequest);

        // Then
        ArgumentCaptor<FleetCompanyEntity> captor = ArgumentCaptor.forClass(FleetCompanyEntity.class);
        verify(fleetCompanyRepository).save(captor.capture());
        FleetCompanyEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("Updated Name Only");
        // Original values should remain
        assertThat(savedEntity.getCorporateEmail()).isEqualTo("old@fleet.com");
        assertThat(savedEntity.getPhone()).isEqualTo("555-0000");
    }

    @Test
    void shouldDeactivateFleet() {
        // Given
        Long companyId = 1L;
        UpdateFleetRequest deactivateRequest = UpdateFleetRequest.builder()
                .isActive(false)
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(fleetCompany));
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(fleetCompany);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(5L);
        when(fleetDtoMapper.toResponse(any(FleetCompanyEntity.class), anyLong())).thenReturn(fleetResponse);

        // When
        updateFleetUseCase.execute(companyId, deactivateRequest);

        // Then
        ArgumentCaptor<FleetCompanyEntity> captor = ArgumentCaptor.forClass(FleetCompanyEntity.class);
        verify(fleetCompanyRepository).save(captor.capture());
        FleetCompanyEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getIsActive()).isFalse();
    }
}
