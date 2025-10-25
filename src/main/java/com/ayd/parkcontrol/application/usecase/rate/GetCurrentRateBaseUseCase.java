package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentRateBaseUseCase {

    private final JpaRateBaseHistoryRepository rateRepository;
    private final RateDtoMapper mapper;

    @Transactional(readOnly = true)
    public RateBaseResponse execute() {
        RateBaseHistoryEntity entity = rateRepository.findCurrentRate()
                .orElseThrow(() -> new RuntimeException("No current rate found"));

        RateBase domain = mapper.toDomain(entity);
        return mapper.toRateBaseResponse(domain);
    }
}
