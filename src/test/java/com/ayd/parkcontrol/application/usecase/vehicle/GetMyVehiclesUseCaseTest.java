package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyVehiclesUseCaseTest {

    @Mock
    private JpaVehicleRepository vehicleRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleDtoMapper mapper;

    @InjectMocks
    private GetMyVehiclesUseCase getMyVehiclesUseCase;

    @Test
    void execute_shouldReturnUserVehicles() {
        Long userId = 1L;

        VehicleEntity vehicle1 = VehicleEntity.builder()
                .id(1L)
                .userId(userId)
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        VehicleEntity vehicle2 = VehicleEntity.builder()
                .id(2L)
                .userId(userId)
                .licensePlate("XYZ-789")
                .vehicleTypeId(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        VehicleTypeEntity vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        when(vehicleRepository.findByUserIdAndIsActive(userId, true))
                .thenReturn(Arrays.asList(vehicle1, vehicle2));
        when(vehicleTypeRepository.findAll()).thenReturn(Arrays.asList(vehicleType));
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class)))
                .thenReturn(VehicleResponse.builder().build());

        List<VehicleResponse> result = getMyVehiclesUseCase.execute(userId);

        assertThat(result).hasSize(2);
        verify(vehicleRepository).findByUserIdAndIsActive(userId, true);
        verify(mapper, times(2)).toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class));
    }
}
