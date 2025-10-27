package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionPlanRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddVehicleToFleetUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private JpaFleetVehicleRepository fleetVehicleRepository;

    @Mock
    private JpaSubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private AddVehicleToFleetUseCase addVehicleToFleetUseCase;

    private FleetCompanyEntity company;
    private SubscriptionPlanEntity plan;
    private VehicleTypeEntity vehicleType;
    private AddVehicleToFleetRequest request;

    @BeforeEach
    void setUp() {
        company = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Company")
                .taxId("12345678-9")
                .corporateEmail("test@company.com")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .build();

        plan = SubscriptionPlanEntity.builder()
                .id(1L)
                .monthlyHours(100)
                .isActive(true)
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("4 Ruedas")
                .build();

        request = AddVehicleToFleetRequest.builder()
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .planId(1L)
                .assignedEmployee("John Doe")
                .build();
    }

    @Test
    void shouldAddVehicleToFleetSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(10L);
        when(subscriptionPlanRepository.existsById(1L)).thenReturn(true);
        when(vehicleTypeRepository.existsById(1)).thenReturn(true);
        when(fleetVehicleRepository.existsActiveLicensePlate("ABC-123")).thenReturn(false);
        when(fleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, "ABC-123")).thenReturn(false);

        FleetVehicleEntity savedVehicle = FleetVehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .companyId(companyId)
                .planId(1L)
                .vehicleTypeId(1)
                .assignedEmployee("John Doe")
                .isActive(true)
                .build();

        FleetVehicleEntity vehicleWithDetails = FleetVehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .company(company)
                .plan(plan)
                .vehicleType(vehicleType)
                .assignedEmployee("John Doe")
                .isActive(true)
                .build();

        FleetVehicleResponse expectedResponse = FleetVehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .assignedEmployee("John Doe")
                .build();

        when(fleetVehicleRepository.save(any(FleetVehicleEntity.class))).thenReturn(savedVehicle);
        when(fleetVehicleRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(vehicleWithDetails));
        when(fleetDtoMapper.toVehicleResponse(vehicleWithDetails)).thenReturn(expectedResponse);

        // When
        FleetVehicleResponse response = addVehicleToFleetUseCase.execute(companyId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(response.getAssignedEmployee()).isEqualTo("John Doe");

        ArgumentCaptor<FleetVehicleEntity> captor = ArgumentCaptor.forClass(FleetVehicleEntity.class);
        verify(fleetVehicleRepository).save(captor.capture());
        FleetVehicleEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(savedEntity.getIsActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addVehicleToFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPlateLimitReached() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(subscriptionPlanRepository.existsById(1L)).thenReturn(true);
        when(vehicleTypeRepository.existsById(1)).thenReturn(true);
        when(fleetVehicleRepository.existsActiveLicensePlate("ABC-123")).thenReturn(false);
        when(fleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, "ABC-123")).thenReturn(false);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(20L); // Limit reached

        // When & Then
        assertThatThrownBy(() -> addVehicleToFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("plate limit");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenLicensePlateAlreadyExists() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(subscriptionPlanRepository.existsById(1L)).thenReturn(true);
        when(vehicleTypeRepository.existsById(1)).thenReturn(true);
        when(fleetVehicleRepository.existsActiveLicensePlate("ABC-123")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> addVehicleToFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPlanNotFound() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(subscriptionPlanRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> addVehicleToFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("plan");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenVehicleTypeNotFound() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(subscriptionPlanRepository.existsById(1L)).thenReturn(true);
        when(vehicleTypeRepository.existsById(1)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> addVehicleToFleetUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle type");

        verify(fleetVehicleRepository, never()).save(any());
    }
}
