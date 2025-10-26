package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetVehicleByIdUseCaseTest {

    @Mock
    private JpaVehicleRepository vehicleRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleDtoMapper mapper;

    @InjectMocks
    private GetVehicleByIdUseCase getVehicleByIdUseCase;

    private VehicleEntity vehicleEntity;
    private VehicleTypeEntity vehicleType;
    private Long vehicleId;

    @BeforeEach
    void setUp() {
        vehicleId = 1L;

        vehicleEntity = VehicleEntity.builder()
                .id(vehicleId)
                .userId(1L)
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();
    }

    @Test
    void execute_shouldReturnVehicleSuccessfully() {
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class)))
                .thenReturn(VehicleResponse.builder().id(vehicleId).build());

        VehicleResponse result = getVehicleByIdUseCase.execute(vehicleId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(vehicleId);
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleTypeRepository).findById(1);
    }

    @Test
    void execute_shouldThrowVehicleNotFoundException_whenVehicleDoesNotExist() {
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getVehicleByIdUseCase.execute(vehicleId))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle not found with ID: " + vehicleId);

        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleTypeRepository, never()).findById(anyInt());
    }
}
