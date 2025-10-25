package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.CreateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateRateBaseUseCase {

    private final JpaRateBaseHistoryRepository rateRepository;
    private final JpaUserRepository userRepository;
    private final RateDtoMapper mapper;

    @Transactional
    public RateBaseResponse execute(CreateRateBaseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RateBaseHistoryEntity> activeRates = rateRepository.findAllActive();
        LocalDateTime now = LocalDateTime.now();

        for (RateBaseHistoryEntity oldRate : activeRates) {
            oldRate.setIsActive(false);
            oldRate.setEndDate(now);
            rateRepository.save(oldRate);
        }

        RateBaseHistoryEntity newRate = RateBaseHistoryEntity.builder()
                .amountPerHour(request.getAmountPerHour())
                .startDate(now)
                .endDate(null)
                .isActive(true)
                .createdBy(user)
                .createdAt(now)
                .build();

        RateBaseHistoryEntity savedRate = rateRepository.save(newRate);
        RateBase domain = mapper.toDomain(savedRate);
        return mapper.toRateBaseResponse(domain);
    }
}
