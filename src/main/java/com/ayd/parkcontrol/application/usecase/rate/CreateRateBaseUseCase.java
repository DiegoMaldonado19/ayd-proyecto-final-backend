package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.CreateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.exception.ActiveRateBaseExistsException;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateRateBaseUseCase {

    private final JpaRateBaseHistoryRepository rateRepository;
    private final JpaUserRepository userRepository;
    private final RateDtoMapper mapper;

    @Transactional
    public RateBaseResponse execute(CreateRateBaseRequest request) {
        log.info("Creating new base rate with amount: {}", request.getAmountPerHour());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            List<RateBaseHistoryEntity> activeRates = rateRepository.findAllActive();
            LocalDateTime now = LocalDateTime.now();

            // Desactivar tarifas anteriores
            for (RateBaseHistoryEntity oldRate : activeRates) {
                oldRate.setIsActive(false);
                oldRate.setEndDate(now);
                rateRepository.save(oldRate);
            }

            // Crear nueva tarifa
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
            
            log.info("Base rate created successfully with ID: {}", savedRate.getId());
            return mapper.toRateBaseResponse(domain);
            
        } catch (DataAccessResourceFailureException e) {
            log.error("Database constraint error when creating base rate: {}", e.getMessage());
            
            // Capturar el error específico del trigger
            if (e.getMessage() != null && e.getMessage().contains("Solo puede existir una tarifa base activa")) {
                throw new ActiveRateBaseExistsException();
            }
            
            // Re-lanzar la excepción original si no es el error esperado
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating base rate: {}", e.getMessage(), e);
            throw e;
        }
    }
}
