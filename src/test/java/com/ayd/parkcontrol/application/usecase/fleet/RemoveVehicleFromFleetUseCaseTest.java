package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveVehicleFromFleetUseCaseTest {

    @Mock
    private JpaFleetVehicleRepository fleetVehicleRepository;

    @InjectMocks
    private RemoveVehicleFromFleetUseCase removeVehicleFromFleetUseCase;

    private FleetVehicleEntity activeVehicle;

    @BeforeEach
    void setUp() {
        activeVehicle = FleetVehicleEntity.builder()
                .id(1L)
                .companyId(1L)
                .licensePlate("ABC-123")
                .planId(1L)
                .vehicleTypeId(1)
                .assignedEmployee("John Doe")
                .isActive(true)
                .build();
    }

    @Test
    void shouldRemoveVehicleSuccessfully() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 1L;
        when(fleetVehicleRepository.findById(vehicleId)).thenReturn(Optional.of(activeVehicle));

        // When
        removeVehicleFromFleetUseCase.execute(companyId, vehicleId);

        // Then
        ArgumentCaptor<FleetVehicleEntity> captor = ArgumentCaptor.forClass(FleetVehicleEntity.class);
        verify(fleetVehicleRepository).save(captor.capture());
        FleetVehicleEntity savedVehicle = captor.getValue();
        assertThat(savedVehicle.getIsActive()).isFalse();
        assertThat(savedVehicle.getId()).isEqualTo(vehicleId);
    }

    @Test
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 999L;
        when(fleetVehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> removeVehicleFromFleetUseCase.execute(companyId, vehicleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenVehicleDoesNotBelongToCompany() {
        // Given
        Long companyId = 2L; // Different company
        Long vehicleId = 1L;
        when(fleetVehicleRepository.findById(vehicleId)).thenReturn(Optional.of(activeVehicle));

        // When & Then
        assertThatThrownBy(() -> removeVehicleFromFleetUseCase.execute(companyId, vehicleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");

        verify(fleetVehicleRepository, never()).save(any());
    }

    @Test
    void shouldRemoveAlreadyInactiveVehicle() {
        // Given
        Long companyId = 1L;
        Long vehicleId = 1L;
        FleetVehicleEntity inactiveVehicle = FleetVehicleEntity.builder()
                .id(vehicleId)
                .companyId(companyId)
                .licensePlate("ABC-123")
                .isActive(false)
                .build();
        when(fleetVehicleRepository.findById(vehicleId)).thenReturn(Optional.of(inactiveVehicle));

        // When
        removeVehicleFromFleetUseCase.execute(companyId, vehicleId);

        // Then
        ArgumentCaptor<FleetVehicleEntity> captor = ArgumentCaptor.forClass(FleetVehicleEntity.class);
        verify(fleetVehicleRepository).save(captor.capture());
        FleetVehicleEntity savedVehicle = captor.getValue();
        assertThat(savedVehicle.getIsActive()).isFalse();
    }
}
