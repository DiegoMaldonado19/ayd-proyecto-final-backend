package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for retrieving fleet consumption statistics
 */
@Service
@RequiredArgsConstructor
public class GetFleetConsumptionUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final JpaTicketRepository ticketRepository;

    /**
     * Retrieves consumption statistics for a fleet company
     *
     * @param companyId the fleet company ID
     * @return fleet consumption statistics
     * @throws IllegalArgumentException if company not found
     */
    @Transactional(readOnly = true)
    public FleetConsumptionResponse execute(Long companyId) {
        FleetCompanyEntity company = fleetCompanyRepository.findById(companyId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Empresa flotillera no encontrada con ID: " + companyId));

        // Get all vehicle license plates from this fleet
        List<String> fleetLicensePlates = ticketRepository.findLicensePlatesByFleetCompany(companyId);

        if (fleetLicensePlates.isEmpty()) {
            return buildEmptyConsumption(company);
        }

        // Get consumption statistics
        Long totalVehicles = fleetCompanyRepository.countActiveVehiclesByCompanyId(companyId);
        Long totalEntries = ticketRepository.countEntriesByLicensePlates(fleetLicensePlates);
        BigDecimal totalHoursConsumed = ticketRepository.sumHoursByLicensePlates(fleetLicensePlates);
        BigDecimal totalAmountCharged = ticketRepository.sumAmountByLicensePlates(fleetLicensePlates);

        // Get per-vehicle consumption details
        List<FleetConsumptionResponse.VehicleConsumptionDetail> vehicleDetails = fleetLicensePlates.stream()
                .map(plate -> {
                    Long entries = ticketRepository.countEntriesByLicensePlate(plate);
                    BigDecimal hours = ticketRepository.sumHoursByLicensePlate(plate);
                    BigDecimal amount = ticketRepository.sumAmountByLicensePlate(plate);

                    return FleetConsumptionResponse.VehicleConsumptionDetail.builder()
                            .licensePlate(plate)
                            .entriesCount(entries != null ? entries : 0L)
                            .hoursConsumed(hours != null ? hours : BigDecimal.ZERO)
                            .amountCharged(amount != null ? amount : BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());

        return FleetConsumptionResponse.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .totalVehicles(totalVehicles != null ? totalVehicles.intValue() : 0)
                .totalEntries(totalEntries != null ? totalEntries : 0L)
                .totalHoursConsumed(totalHoursConsumed != null ? totalHoursConsumed : BigDecimal.ZERO)
                .totalAmountCharged(totalAmountCharged != null ? totalAmountCharged : BigDecimal.ZERO)
                .vehicleConsumption(vehicleDetails)
                .build();
    }

    private FleetConsumptionResponse buildEmptyConsumption(FleetCompanyEntity company) {
        return FleetConsumptionResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .totalVehicles(0)
                .totalEntries(0L)
                .totalHoursConsumed(BigDecimal.ZERO)
                .totalAmountCharged(BigDecimal.ZERO)
                .vehicleConsumption(List.of())
                .build();
    }
}
