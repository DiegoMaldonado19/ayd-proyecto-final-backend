package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateOccupancyReportUseCase Tests")
class GenerateOccupancyReportUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @InjectMocks
    private GenerateOccupancyReportUseCase generateOccupancyReportUseCase;

    private TicketStatusTypeEntity inProgressStatus;
    private VehicleTypeEntity vehicleType2R;
    private VehicleTypeEntity vehicleType4R;
    private Branch branch1;
    private Branch branch2;

    @BeforeEach
    void setUp() {
        inProgressStatus = new TicketStatusTypeEntity();
        inProgressStatus.setId(1);
        inProgressStatus.setCode("IN_PROGRESS");

        vehicleType2R = new VehicleTypeEntity();
        vehicleType2R.setId(1);
        vehicleType2R.setCode("2R");

        vehicleType4R = new VehicleTypeEntity();
        vehicleType4R.setId(2);
        vehicleType4R.setCode("4R");

        branch1 = Branch.builder()
                .id(1L)
                .name("Sucursal Centro")
                .capacity2r(50)
                .capacity4r(30)
                .ratePerHour(BigDecimal.valueOf(20.00))
                .build();

        branch2 = Branch.builder()
                .id(2L)
                .name("Sucursal Norte")
                .capacity2r(40)
                .capacity4r(25)
                .ratePerHour(BigDecimal.valueOf(18.00))
                .build();
    }

    @Test
    @DisplayName("Debe generar reporte de ocupación exitosamente")
    void shouldGenerateOccupancyReportSuccessfully() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R"))
                .thenReturn(Optional.of(vehicleType4R));
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(branch1, branch2)));

        // Sucursal 1: 10 motos de 50, 15 autos de 30
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 1, 1))
                .thenReturn(10L);
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1))
                .thenReturn(15L);

        // Sucursal 2: 20 motos de 40, 10 autos de 25
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(2L, 1, 1))
                .thenReturn(20L);
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(2L, 2, 1))
                .thenReturn(10L);

        // Act
        var result = generateOccupancyReportUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size()); // 2 sucursales x 2 tipos de vehículo

        // Verificar Sucursal Centro - 2R
        var centro2R = result.stream()
                .filter(r -> r.getBranchId().equals(1L) && "2R".equals(r.getVehicleType()))
                .findFirst()
                .orElseThrow();

        assertEquals("Sucursal Centro", centro2R.getBranchName());
        assertEquals(50, centro2R.getTotalCapacity());
        assertEquals(10, centro2R.getCurrentOccupancy());
        assertEquals(20.0, centro2R.getOccupancyPercentage());

        // Verificar Sucursal Norte - 4R
        var norte4R = result.stream()
                .filter(r -> r.getBranchId().equals(2L) && "4R".equals(r.getVehicleType()))
                .findFirst()
                .orElseThrow();

        assertEquals("Sucursal Norte", norte4R.getBranchName());
        assertEquals(25, norte4R.getTotalCapacity());
        assertEquals(10, norte4R.getCurrentOccupancy());
        assertEquals(40.0, norte4R.getOccupancyPercentage());

        verify(ticketStatusTypeRepository).findByCode("IN_PROGRESS");
        verify(vehicleTypeRepository).findByCode("2R");
        verify(vehicleTypeRepository).findByCode("4R");
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay sucursales")
    void shouldReturnEmptyListWhenNoBranches() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R"))
                .thenReturn(Optional.of(vehicleType4R));
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Act
        var result = generateOccupancyReportUseCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe manejar sucursales con ocupación 100%")
    void shouldHandleFullOccupancy() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R"))
                .thenReturn(Optional.of(vehicleType4R));
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(branch1)));

        // 100% ocupación en 2R
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 1, 1))
                .thenReturn(50L);
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1))
                .thenReturn(30L);

        // Act
        var result = generateOccupancyReportUseCase.execute();

        // Assert
        assertNotNull(result);
        var detail2R = result.stream()
                .filter(r -> "2R".equals(r.getVehicleType()))
                .findFirst()
                .orElseThrow();

        assertEquals(100.0, detail2R.getOccupancyPercentage());
        assertEquals(50, detail2R.getCurrentOccupancy());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra estado IN_PROGRESS")
    void shouldThrowExceptionWhenStatusNotFound() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> generateOccupancyReportUseCase.execute());
    }
}
