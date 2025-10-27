package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFleetVehiclesUseCaseTest {

    @Mock
    private JpaFleetVehicleRepository fleetVehicleRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private ListFleetVehiclesUseCase listFleetVehiclesUseCase;

    private Pageable pageable;
    private List<FleetVehicleEntity> vehicleEntities;
    private List<FleetVehicleResponse> vehicleResponses;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        vehicleEntities = Arrays.asList(
                FleetVehicleEntity.builder()
                        .id(1L)
                        .companyId(1L)
                        .licensePlate("ABC-123")
                        .isActive(true)
                        .build(),
                FleetVehicleEntity.builder()
                        .id(2L)
                        .companyId(1L)
                        .licensePlate("XYZ-789")
                        .isActive(true)
                        .build());

        vehicleResponses = Arrays.asList(
                FleetVehicleResponse.builder()
                        .id(1L)
                        .licensePlate("ABC-123")
                        .build(),
                FleetVehicleResponse.builder()
                        .id(2L)
                        .licensePlate("XYZ-789")
                        .build());
    }

    @Test
    void shouldListAllVehiclesForCompany() {
        // Given
        Long companyId = 1L;
        Page<FleetVehicleEntity> entityPage = new PageImpl<>(vehicleEntities, pageable, vehicleEntities.size());

        when(fleetVehicleRepository.findByCompanyId(companyId, pageable)).thenReturn(entityPage);
        when(fleetDtoMapper.toVehicleResponse(any(FleetVehicleEntity.class)))
                .thenReturn(vehicleResponses.get(0), vehicleResponses.get(1));

        // When
        Page<FleetVehicleResponse> result = listFleetVehiclesUseCase.execute(companyId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoVehicles() {
        // Given
        Long companyId = 1L;
        Page<FleetVehicleEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(fleetVehicleRepository.findByCompanyId(companyId, pageable)).thenReturn(emptyPage);

        // When
        Page<FleetVehicleResponse> result = listFleetVehiclesUseCase.execute(companyId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldListActiveVehiclesOnly() {
        // Given
        Long companyId = 1L;
        Boolean isActive = true;
        Page<FleetVehicleEntity> entityPage = new PageImpl<>(vehicleEntities, pageable, vehicleEntities.size());

        when(fleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive, pageable))
                .thenReturn(entityPage);
        when(fleetDtoMapper.toVehicleResponse(any(FleetVehicleEntity.class)))
                .thenReturn(vehicleResponses.get(0), vehicleResponses.get(1));

        // When
        Page<FleetVehicleResponse> result = listFleetVehiclesUseCase.executeByActive(companyId, isActive, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldListInactiveVehicles() {
        // Given
        Long companyId = 1L;
        Boolean isActive = false;
        FleetVehicleEntity inactiveVehicle = FleetVehicleEntity.builder()
                .id(3L)
                .companyId(companyId)
                .licensePlate("DEF-456")
                .isActive(false)
                .build();
        Page<FleetVehicleEntity> entityPage = new PageImpl<>(
                Collections.singletonList(inactiveVehicle), pageable, 1);
        FleetVehicleResponse inactiveResponse = FleetVehicleResponse.builder()
                .id(3L)
                .licensePlate("DEF-456")
                .build();

        when(fleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive, pageable))
                .thenReturn(entityPage);
        when(fleetDtoMapper.toVehicleResponse(inactiveVehicle)).thenReturn(inactiveResponse);

        // When
        Page<FleetVehicleResponse> result = listFleetVehiclesUseCase.executeByActive(companyId, isActive, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLicensePlate()).isEqualTo("DEF-456");
    }
}
