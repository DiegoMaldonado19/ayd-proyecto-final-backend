package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRateBaseHistoryUseCase {

    private final JpaRateBaseHistoryRepository rateRepository;
    private final RateDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<RateBaseResponse> execute() {
        return rateRepository.findAllOrderByStartDateDesc()
                .stream()
                .map(mapper::toDomain)
                .map(mapper::toRateBaseResponse)
                .collect(Collectors.toList());
    }
}
