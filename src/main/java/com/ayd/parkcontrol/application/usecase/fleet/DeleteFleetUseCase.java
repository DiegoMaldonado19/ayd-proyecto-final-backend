package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFleetUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;

    @Transactional
    public void execute(Long id) {
        log.info("Deleting fleet company with id: {}", id);

        if (!fleetCompanyRepository.existsById(id)) {
            throw new IllegalArgumentException("Empresa flotillera no encontrada con ID: " + id);
        }

        Long activeVehiclesCount = fleetCompanyRepository.countActiveVehiclesByCompanyId(id);
        if (activeVehiclesCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete fleet company with active vehicles. Count: " + activeVehiclesCount);
        }

        fleetCompanyRepository.deleteById(id);
        log.info("Fleet company deleted successfully with id: {}", id);
    }
}
