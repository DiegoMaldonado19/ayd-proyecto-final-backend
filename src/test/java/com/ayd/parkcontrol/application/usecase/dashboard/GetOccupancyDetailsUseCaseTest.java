package com.ayd.parkcontrol.application.usecase.dashboard;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOccupancyDetailsUseCase Tests")
class GetOccupancyDetailsUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @InjectMocks
    private GetOccupancyDetailsUseCase getOccupancyDetailsUseCase;

    private TicketStatusTypeEntity inProgressStatus;
    private VehicleTypeEntity vehicleType2R;
    private VehicleTypeEntity vehicleType4R;
    private Branch branch;

    @BeforeEach
    void setUp() {
        inProgressStatus = new TicketStatusTypeEntity();
        inProgressStatus.setId(1);
        inProgressStatus.setCode("IN_PROGRESS");

        vehicleType2R = new VehicleTypeEntity();
        vehicleType2R.setId(1);
        vehicleType2R.setCode("2R");
        vehicleType2R.setName("Motocicleta");

        vehicleType4R = new VehicleTypeEntity();
        vehicleType4R.setId(2);
        vehicleType4R.setCode("4R");
        vehicleType4R.setName("Automóvil");

        branch = Branch.builder()
                .id(1L)
                .name("Sucursal Centro")
                .capacity2r(50)
                .capacity4r(30)
                .ratePerHour(BigDecimal.valueOf(20.00))
                .build();
    }

    @Test
    @DisplayName("Debe obtener detalles de ocupación exitosamente")
    void shouldGetOccupancyDetailsSuccessfully() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R"))
                .thenReturn(Optional.of(vehicleType4R));
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(branch)));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), eq(1), eq(1)))
                .thenReturn(10L); // 10 motos ocupadas
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), eq(2), eq(1)))
                .thenReturn(15L); // 15 autos ocupados

        // Act
        var result = getOccupancyDetailsUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Dos entradas: una para 2R y otra para 4R

        var detail2R = result.stream()
                .filter(d -> "2R".equals(d.getVehicleType()))
                .findFirst()
                .orElseThrow();
        assertEquals(1L, detail2R.getBranchId());
        assertEquals("Sucursal Centro", detail2R.getBranchName());
        assertEquals(50, detail2R.getTotalCapacity());
        assertEquals(10, detail2R.getCurrentOccupancy());
        assertEquals(40, detail2R.getAvailableSpaces());
        assertEquals(20.0, detail2R.getOccupancyPercentage());

        var detail4R = result.stream()
                .filter(d -> "4R".equals(d.getVehicleType()))
                .findFirst()
                .orElseThrow();
        assertEquals(30, detail4R.getTotalCapacity());
        assertEquals(15, detail4R.getCurrentOccupancy());
        assertEquals(50.0, detail4R.getOccupancyPercentage());
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
        var result = getOccupancyDetailsUseCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe omitir sucursales sin capacidad")
    void shouldSkipBranchesWithoutCapacity() {
        // Arrange
        Branch branchNoCapacity = Branch.builder()
                .id(2L)
                .name("Sucursal Sin Capacidad")
                .capacity2r(0)
                .capacity4r(0)
                .build();

        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R"))
                .thenReturn(Optional.of(vehicleType4R));
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(branchNoCapacity)));

        // Act
        var result = getOccupancyDetailsUseCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra tipo de vehículo")
    void shouldThrowExceptionWhenVehicleTypeNotFound() {
        // Arrange
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> getOccupancyDetailsUseCase.execute());
    }
}
