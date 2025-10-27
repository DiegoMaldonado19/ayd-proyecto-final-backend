package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFleetConsumptionUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @InjectMocks
    private GetFleetConsumptionUseCase getFleetConsumptionUseCase;

    private FleetCompanyEntity company;
    private List<String> licensePlates;

    @BeforeEach
    void setUp() {
        company = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Fleet Company")
                .taxId("12345678-9")
                .corporateEmail("test@fleet.com")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .build();

        licensePlates = Arrays.asList("ABC-123", "DEF-456", "GHI-789");
    }

    @Test
    void shouldGetFleetConsumptionSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(ticketRepository.findLicensePlatesByFleetCompany(companyId)).thenReturn(licensePlates);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(3L);
        when(ticketRepository.countEntriesByLicensePlates(licensePlates)).thenReturn(150L);
        when(ticketRepository.sumHoursByLicensePlates(licensePlates)).thenReturn(new BigDecimal("450.50"));
        when(ticketRepository.sumAmountByLicensePlates(licensePlates)).thenReturn(new BigDecimal("22525.00"));

        // Mock per-vehicle consumption
        when(ticketRepository.countEntriesByLicensePlate("ABC-123")).thenReturn(50L);
        when(ticketRepository.sumHoursByLicensePlate("ABC-123")).thenReturn(new BigDecimal("150.00"));
        when(ticketRepository.sumAmountByLicensePlate("ABC-123")).thenReturn(new BigDecimal("7500.00"));

        when(ticketRepository.countEntriesByLicensePlate("DEF-456")).thenReturn(60L);
        when(ticketRepository.sumHoursByLicensePlate("DEF-456")).thenReturn(new BigDecimal("180.25"));
        when(ticketRepository.sumAmountByLicensePlate("DEF-456")).thenReturn(new BigDecimal("9012.50"));

        when(ticketRepository.countEntriesByLicensePlate("GHI-789")).thenReturn(40L);
        when(ticketRepository.sumHoursByLicensePlate("GHI-789")).thenReturn(new BigDecimal("120.25"));
        when(ticketRepository.sumAmountByLicensePlate("GHI-789")).thenReturn(new BigDecimal("6012.50"));

        // When
        FleetConsumptionResponse response = getFleetConsumptionUseCase.execute(companyId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCompanyId()).isEqualTo(companyId);
        assertThat(response.getCompanyName()).isEqualTo("Test Fleet Company");
        assertThat(response.getTotalVehicles()).isEqualTo(3);
        assertThat(response.getTotalEntries()).isEqualTo(150L);
        assertThat(response.getTotalHoursConsumed()).isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(response.getTotalAmountCharged()).isEqualByComparingTo(new BigDecimal("22525.00"));
        assertThat(response.getVehicleConsumption()).hasSize(3);

        verify(fleetCompanyRepository).findById(companyId);
        verify(ticketRepository).findLicensePlatesByFleetCompany(companyId);
        verify(fleetCompanyRepository).countActiveVehiclesByCompanyId(companyId);
        verify(ticketRepository).countEntriesByLicensePlates(licensePlates);
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        Long companyId = 999L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getFleetConsumptionUseCase.execute(companyId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Empresa flotillera no encontrada con ID: 999");

        verify(fleetCompanyRepository).findById(companyId);
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void shouldReturnEmptyConsumptionWhenNoVehicles() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(ticketRepository.findLicensePlatesByFleetCompany(companyId)).thenReturn(List.of());

        // When
        FleetConsumptionResponse response = getFleetConsumptionUseCase.execute(companyId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCompanyId()).isEqualTo(companyId);
        assertThat(response.getCompanyName()).isEqualTo("Test Fleet Company");
        assertThat(response.getTotalVehicles()).isZero();
        assertThat(response.getTotalEntries()).isZero();
        assertThat(response.getTotalHoursConsumed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalAmountCharged()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getVehicleConsumption()).isEmpty();

        verify(fleetCompanyRepository).findById(companyId);
        verify(ticketRepository).findLicensePlatesByFleetCompany(companyId);
        verify(fleetCompanyRepository, never()).countActiveVehiclesByCompanyId(anyLong());
    }

    @Test
    void shouldHandleNullValuesInConsumptionData() {
        // Given
        Long companyId = 1L;
        List<String> singlePlate = List.of("ABC-123");
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(ticketRepository.findLicensePlatesByFleetCompany(companyId)).thenReturn(singlePlate);
        when(fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId)).thenReturn(null);
        when(ticketRepository.countEntriesByLicensePlates(singlePlate)).thenReturn(null);
        when(ticketRepository.sumHoursByLicensePlates(singlePlate)).thenReturn(null);
        when(ticketRepository.sumAmountByLicensePlates(singlePlate)).thenReturn(null);

        when(ticketRepository.countEntriesByLicensePlate("ABC-123")).thenReturn(null);
        when(ticketRepository.sumHoursByLicensePlate("ABC-123")).thenReturn(null);
        when(ticketRepository.sumAmountByLicensePlate("ABC-123")).thenReturn(null);

        // When
        FleetConsumptionResponse response = getFleetConsumptionUseCase.execute(companyId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalVehicles()).isZero();
        assertThat(response.getTotalEntries()).isZero();
        assertThat(response.getTotalHoursConsumed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalAmountCharged()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getVehicleConsumption()).hasSize(1);

        var vehicleDetail = response.getVehicleConsumption().get(0);
        assertThat(vehicleDetail.getEntriesCount()).isZero();
        assertThat(vehicleDetail.getHoursConsumed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vehicleDetail.getAmountCharged()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
