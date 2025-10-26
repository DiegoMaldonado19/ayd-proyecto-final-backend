package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteVehicleUseCaseTest {

    @Mock
    private JpaVehicleRepository vehicleRepository;

    @InjectMocks
    private DeleteVehicleUseCase deleteVehicleUseCase;

    private VehicleEntity vehicleEntity;
    private Long userId;
    private Long vehicleId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        vehicleId = 1L;

        vehicleEntity = VehicleEntity.builder()
                .id(vehicleId)
                .userId(userId)
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_shouldSoftDeleteVehicleSuccessfully() {
        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(vehicleEntity));

        deleteVehicleUseCase.execute(vehicleId, userId);

        verify(vehicleRepository).findByIdAndUserId(vehicleId, userId);
        verify(vehicleRepository).save(any(VehicleEntity.class));
    }

    @Test
    void execute_shouldThrowVehicleNotFoundException_whenVehicleDoesNotExist() {
        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteVehicleUseCase.execute(vehicleId, userId))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle not found with ID: " + vehicleId);

        verify(vehicleRepository, never()).save(any(VehicleEntity.class));
    }
}
