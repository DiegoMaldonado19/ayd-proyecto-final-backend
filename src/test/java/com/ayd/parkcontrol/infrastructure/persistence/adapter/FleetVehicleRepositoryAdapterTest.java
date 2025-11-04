package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.FleetVehicleEntityMapper;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetVehicleRepositoryAdapterTest {

    @Mock
    private JpaFleetVehicleRepository jpaFleetVehicleRepository;

    @Mock
    private FleetVehicleEntityMapper fleetVehicleEntityMapper;

    @InjectMocks
    private FleetVehicleRepositoryAdapter fleetVehicleRepositoryAdapter;

    private FleetVehicle fleetVehicle;
    private FleetVehicleEntity fleetVehicleEntity;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        fleetVehicle = FleetVehicle.builder()
                .id(1L)
                .companyId(1L)
                .licensePlate("ABC123")
                .vehicleType("sedan")
                .brand("Toyota")
                .model("Corolla")
                .color("Blue")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        fleetVehicleEntity = new FleetVehicleEntity();
        fleetVehicleEntity.setId(1L);
        fleetVehicleEntity.setLicensePlate("ABC123");
        fleetVehicleEntity.setIsActive(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByCompanyId_shouldReturnPageOfFleetVehicles() {
        // Given
        Long companyId = 1L;
        Page<FleetVehicleEntity> entityPage = new PageImpl<>(Collections.singletonList(fleetVehicleEntity));
        when(jpaFleetVehicleRepository.findByCompanyId(companyId, pageable))
                .thenReturn(entityPage);
        when(fleetVehicleEntityMapper.toDomain(fleetVehicleEntity))
                .thenReturn(fleetVehicle);

        // When
        Page<FleetVehicle> result = fleetVehicleRepositoryAdapter.findByCompanyId(companyId, pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(fleetVehicle);
        verify(jpaFleetVehicleRepository).findByCompanyId(companyId, pageable);
    }

    @Test
    void findByCompanyId_shouldReturnEmptyPage_whenNoVehicles() {
        // Given
        Long companyId = 999L;
        Page<FleetVehicleEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(jpaFleetVehicleRepository.findByCompanyId(companyId, pageable))
                .thenReturn(emptyPage);

        // When
        Page<FleetVehicle> result = fleetVehicleRepositoryAdapter.findByCompanyId(companyId, pageable);

        // Then
        assertThat(result).isEmpty();
        verify(jpaFleetVehicleRepository).findByCompanyId(companyId, pageable);
    }

    @Test
    void findByCompanyIdAndIsActive_shouldReturnPageOfActiveVehicles() {
        // Given
        Long companyId = 1L;
        Boolean isActive = true;
        Page<FleetVehicleEntity> entityPage = new PageImpl<>(Collections.singletonList(fleetVehicleEntity));
        when(jpaFleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive, pageable))
                .thenReturn(entityPage);
        when(fleetVehicleEntityMapper.toDomain(fleetVehicleEntity))
                .thenReturn(fleetVehicle);

        // When
        Page<FleetVehicle> result = fleetVehicleRepositoryAdapter.findByCompanyIdAndIsActive(companyId, isActive,
                pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
        verify(jpaFleetVehicleRepository).findByCompanyIdAndIsActive(companyId, isActive, pageable);
    }

    @Test
    void findByCompanyIdAndIsActive_shouldReturnPageOfInactiveVehicles() {
        // Given
        Long companyId = 1L;
        Boolean isActive = false;
        FleetVehicle inactiveVehicle = FleetVehicle.builder()
                .id(2L)
                .companyId(1L)
                .licensePlate("XYZ789")
                .isActive(false)
                .build();
        FleetVehicleEntity inactiveEntity = new FleetVehicleEntity();
        inactiveEntity.setId(2L);
        inactiveEntity.setIsActive(false);

        Page<FleetVehicleEntity> entityPage = new PageImpl<>(Collections.singletonList(inactiveEntity));
        when(jpaFleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive, pageable))
                .thenReturn(entityPage);
        when(fleetVehicleEntityMapper.toDomain(inactiveEntity))
                .thenReturn(inactiveVehicle);

        // When
        Page<FleetVehicle> result = fleetVehicleRepositoryAdapter.findByCompanyIdAndIsActive(companyId, isActive,
                pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsActive()).isFalse();
        verify(jpaFleetVehicleRepository).findByCompanyIdAndIsActive(companyId, isActive, pageable);
    }

    @Test
    void countByCompanyIdAndIsActive_shouldReturnCount() {
        // Given
        Long companyId = 1L;
        Boolean isActive = true;
        long expectedCount = 5L;
        when(jpaFleetVehicleRepository.countByCompanyIdAndIsActive(companyId, isActive))
                .thenReturn(expectedCount);

        // When
        long result = fleetVehicleRepositoryAdapter.countByCompanyIdAndIsActive(companyId, isActive);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(jpaFleetVehicleRepository).countByCompanyIdAndIsActive(companyId, isActive);
    }

    @Test
    void countByCompanyIdAndIsActive_shouldReturnZero_whenNoVehicles() {
        // Given
        Long companyId = 999L;
        Boolean isActive = true;
        when(jpaFleetVehicleRepository.countByCompanyIdAndIsActive(companyId, isActive))
                .thenReturn(0L);

        // When
        long result = fleetVehicleRepositoryAdapter.countByCompanyIdAndIsActive(companyId, isActive);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(jpaFleetVehicleRepository).countByCompanyIdAndIsActive(companyId, isActive);
    }

    @Test
    void existsByCompanyIdAndLicensePlate_shouldReturnTrue_whenExists() {
        // Given
        Long companyId = 1L;
        String licensePlate = "ABC123";
        when(jpaFleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, licensePlate))
                .thenReturn(true);

        // When
        boolean result = fleetVehicleRepositoryAdapter.existsByCompanyIdAndLicensePlate(companyId, licensePlate);

        // Then
        assertThat(result).isTrue();
        verify(jpaFleetVehicleRepository).existsByCompanyIdAndLicensePlate(companyId, licensePlate);
    }

    @Test
    void existsByCompanyIdAndLicensePlate_shouldReturnFalse_whenNotExists() {
        // Given
        Long companyId = 1L;
        String licensePlate = "XYZ999";
        when(jpaFleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, licensePlate))
                .thenReturn(false);

        // When
        boolean result = fleetVehicleRepositoryAdapter.existsByCompanyIdAndLicensePlate(companyId, licensePlate);

        // Then
        assertThat(result).isFalse();
        verify(jpaFleetVehicleRepository).existsByCompanyIdAndLicensePlate(companyId, licensePlate);
    }

    @Test
    void save_shouldSaveAndReturnFleetVehicle() {
        // Given
        when(fleetVehicleEntityMapper.toEntity(fleetVehicle))
                .thenReturn(fleetVehicleEntity);
        when(jpaFleetVehicleRepository.save(fleetVehicleEntity))
                .thenReturn(fleetVehicleEntity);
        when(fleetVehicleEntityMapper.toDomain(fleetVehicleEntity))
                .thenReturn(fleetVehicle);

        // When
        FleetVehicle result = fleetVehicleRepositoryAdapter.save(fleetVehicle);

        // Then
        assertThat(result).isEqualTo(fleetVehicle);
        verify(fleetVehicleEntityMapper).toEntity(fleetVehicle);
        verify(jpaFleetVehicleRepository).save(fleetVehicleEntity);
        verify(fleetVehicleEntityMapper).toDomain(fleetVehicleEntity);
    }

    @Test
    void findById_shouldReturnFleetVehicle_whenExists() {
        // Given
        Long id = 1L;
        when(jpaFleetVehicleRepository.findById(id))
                .thenReturn(Optional.of(fleetVehicleEntity));
        when(fleetVehicleEntityMapper.toDomain(fleetVehicleEntity))
                .thenReturn(fleetVehicle);

        // When
        Optional<FleetVehicle> result = fleetVehicleRepositoryAdapter.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(fleetVehicle);
        verify(jpaFleetVehicleRepository).findById(id);
        verify(fleetVehicleEntityMapper).toDomain(fleetVehicleEntity);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        // Given
        Long id = 999L;
        when(jpaFleetVehicleRepository.findById(id))
                .thenReturn(Optional.empty());

        // When
        Optional<FleetVehicle> result = fleetVehicleRepositoryAdapter.findById(id);

        // Then
        assertThat(result).isEmpty();
        verify(jpaFleetVehicleRepository).findById(id);
        verify(fleetVehicleEntityMapper, never()).toDomain(any());
    }
}
