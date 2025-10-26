package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.UpdateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
import com.ayd.parkcontrol.domain.exception.VehicleTypeNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleUseCaseTest {

    @Mock
    private JpaVehicleRepository vehicleRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleDtoMapper mapper;

    @InjectMocks
    private UpdateVehicleUseCase updateVehicleUseCase;

    private VehicleEntity vehicleEntity;
    private VehicleTypeEntity vehicleType;
    private UpdateVehicleRequest request;
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
                .brand("Toyota")
                .model("Corolla")
                .color("Blue")
                .year(2020)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        request = UpdateVehicleRequest.builder()
                .brand("Honda")
                .model("Civic")
                .build();
    }

    @Test
    void execute_shouldUpdateVehicleSuccessfully() {
        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicleEntity);
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class)))
                .thenReturn(VehicleResponse.builder().id(vehicleId).build());

        VehicleResponse result = updateVehicleUseCase.execute(vehicleId, request, userId);

        assertThat(result).isNotNull();
        verify(vehicleRepository).save(any(VehicleEntity.class));
    }

    @Test
    void execute_shouldThrowVehicleNotFoundException_whenVehicleDoesNotExist() {
        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateVehicleUseCase.execute(vehicleId, request, userId))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void execute_shouldUpdateLicensePlate_whenProvided() {
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .licensePlate("XYZ-789")
                .build();

        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleRepository.existsByUserIdAndLicensePlateAndIdNot(userId, "XYZ-789", vehicleId)).thenReturn(false);
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicleEntity);
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class)))
                .thenReturn(VehicleResponse.builder().id(vehicleId).build());

        updateVehicleUseCase.execute(vehicleId, updateRequest, userId);

        verify(vehicleRepository).existsByUserIdAndLicensePlateAndIdNot(userId, "XYZ-789", vehicleId);
    }

    @Test
    void execute_shouldThrowDuplicateLicensePlateException_whenNewPlateAlreadyExists() {
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .licensePlate("XYZ-789")
                .build();

        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleRepository.existsByUserIdAndLicensePlateAndIdNot(userId, "XYZ-789", vehicleId)).thenReturn(true);

        assertThatThrownBy(() -> updateVehicleUseCase.execute(vehicleId, updateRequest, userId))
                .isInstanceOf(DuplicateLicensePlateException.class);
    }

    @Test
    void execute_shouldThrowVehicleTypeNotFoundException_whenNewVehicleTypeDoesNotExist() {
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .vehicleTypeId(99)
                .build();

        when(vehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleTypeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateVehicleUseCase.execute(vehicleId, updateRequest, userId))
                .isInstanceOf(VehicleTypeNotFoundException.class);
    }
}
