package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.CreateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
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
class CreateVehicleUseCaseTest {

    @Mock
    private JpaVehicleRepository vehicleRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleDtoMapper mapper;

    @InjectMocks
    private CreateVehicleUseCase createVehicleUseCase;

    private CreateVehicleRequest request;
    private VehicleTypeEntity vehicleType;
    private VehicleEntity vehicleEntity;
    private VehicleResponse vehicleResponse;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;

        request = CreateVehicleRequest.builder()
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .brand("Toyota")
                .model("Corolla")
                .color("Blue")
                .year(2020)
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .createdAt(LocalDateTime.now())
                .build();

        vehicleEntity = VehicleEntity.builder()
                .id(1L)
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

        vehicleResponse = VehicleResponse.builder()
                .id(1L)
                .userId(userId)
                .licensePlate("ABC-123")
                .build();
    }

    @Test
    void execute_shouldCreateVehicleSuccessfully() {
        when(vehicleRepository.existsByUserIdAndLicensePlate(userId, "ABC-123")).thenReturn(false);
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicleEntity);
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class))).thenReturn(vehicleResponse);

        VehicleResponse result = createVehicleUseCase.execute(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(vehicleRepository).existsByUserIdAndLicensePlate(userId, "ABC-123");
        verify(vehicleTypeRepository).findById(1);
        verify(vehicleRepository).save(any(VehicleEntity.class));
        verify(mapper).toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class));
    }

    @Test
    void execute_shouldNormalizeLicensePlateToUpperCase() {
        CreateVehicleRequest lowerCaseRequest = CreateVehicleRequest.builder()
                .licensePlate("abc-123")
                .vehicleTypeId(1)
                .build();

        when(vehicleRepository.existsByUserIdAndLicensePlate(userId, "ABC-123")).thenReturn(false);
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicleEntity);
        when(mapper.toResponse(any(VehicleEntity.class), any(VehicleTypeEntity.class))).thenReturn(vehicleResponse);

        createVehicleUseCase.execute(lowerCaseRequest, userId);

        verify(vehicleRepository).existsByUserIdAndLicensePlate(userId, "ABC-123");
    }

    @Test
    void execute_shouldThrowDuplicateLicensePlateException_whenPlateAlreadyExists() {
        when(vehicleRepository.existsByUserIdAndLicensePlate(userId, "ABC-123")).thenReturn(true);

        assertThatThrownBy(() -> createVehicleUseCase.execute(request, userId))
                .isInstanceOf(DuplicateLicensePlateException.class)
                .hasMessageContaining("Vehicle with license plate ABC-123 already exists for this user");

        verify(vehicleRepository).existsByUserIdAndLicensePlate(userId, "ABC-123");
        verify(vehicleTypeRepository, never()).findById(anyInt());
        verify(vehicleRepository, never()).save(any(VehicleEntity.class));
    }

    @Test
    void execute_shouldThrowVehicleTypeNotFoundException_whenVehicleTypeDoesNotExist() {
        when(vehicleRepository.existsByUserIdAndLicensePlate(userId, "ABC-123")).thenReturn(false);
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createVehicleUseCase.execute(request, userId))
                .isInstanceOf(VehicleTypeNotFoundException.class)
                .hasMessageContaining("Vehicle type not found with ID: 1");

        verify(vehicleRepository).existsByUserIdAndLicensePlate(userId, "ABC-123");
        verify(vehicleTypeRepository).findById(1);
        verify(vehicleRepository, never()).save(any(VehicleEntity.class));
    }
}
