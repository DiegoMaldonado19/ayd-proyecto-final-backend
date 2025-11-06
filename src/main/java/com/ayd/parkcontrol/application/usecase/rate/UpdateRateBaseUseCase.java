package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.UpdateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateRateBaseUseCase {

    private final JpaRateBaseHistoryRepository rateRepository;
    private final RateDtoMapper mapper;

    @Transactional
    public RateBaseResponse execute(UpdateRateBaseRequest request) {
        log.info("Updating base rate with new amount: {}", request.getAmountPerHour());

        // Buscar la tarifa base activa actual
        RateBaseHistoryEntity currentRate = rateRepository.findCurrentRate()
                .orElseThrow(() -> new RuntimeException("No active base rate found to update"));

        // Actualizar solo el monto de la tarifa existente
        currentRate.setAmountPerHour(request.getAmountPerHour());

        RateBaseHistoryEntity savedRate = rateRepository.save(currentRate);
        RateBase domain = mapper.toDomain(savedRate);

        log.info("Base rate updated successfully with ID: {}", savedRate.getId());
        return mapper.toRateBaseResponse(domain);
    }
}
