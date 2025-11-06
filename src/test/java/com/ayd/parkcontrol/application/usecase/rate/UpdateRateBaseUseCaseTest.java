package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.UpdateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateRateBaseUseCaseTest {

        @Mock
        private JpaRateBaseHistoryRepository rateRepository;

        @Mock
        private RateDtoMapper mapper;

        @InjectMocks
        private UpdateRateBaseUseCase useCase;

        private UpdateRateBaseRequest request;
        private RateBaseHistoryEntity oldRateEntity;

        @BeforeEach
        void setUp() {
                request = UpdateRateBaseRequest.builder()
                                .amountPerHour(new BigDecimal("7.50"))
                                .build();

                oldRateEntity = RateBaseHistoryEntity.builder()
                                .id(1L)
                                .amountPerHour(new BigDecimal("5.00"))
                                .startDate(LocalDateTime.now().minusDays(10))
                                .endDate(null)
                                .isActive(true)
                                .build();
        }

        @Test
        void execute_shouldUpdateExistingBaseRate() {
                // Given
                RateBaseHistoryEntity updatedRateEntity = RateBaseHistoryEntity.builder()
                                .id(1L)
                                .amountPerHour(new BigDecimal("7.50"))
                                .startDate(oldRateEntity.getStartDate())
                                .endDate(null)
                                .isActive(true)
                                .build();

                RateBase updatedRateDomain = RateBase.builder()
                                .id(1L)
                                .amountPerHour(new BigDecimal("7.50"))
                                .startDate(oldRateEntity.getStartDate())
                                .build();

                RateBaseResponse updatedResponse = RateBaseResponse.builder()
                                .id(1L)
                                .amountPerHour(new BigDecimal("7.50"))
                                .build();

                when(rateRepository.findCurrentRate()).thenReturn(Optional.of(oldRateEntity));
                when(rateRepository.save(any(RateBaseHistoryEntity.class))).thenReturn(updatedRateEntity);
                when(mapper.toDomain(updatedRateEntity)).thenReturn(updatedRateDomain);
                when(mapper.toRateBaseResponse(updatedRateDomain)).thenReturn(updatedResponse);

                // When
                RateBaseResponse result = useCase.execute(request);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(1L);
                assertThat(result.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("7.50"));

                verify(rateRepository, times(1)).findCurrentRate();
                verify(rateRepository, times(1)).save(any(RateBaseHistoryEntity.class));
                verify(mapper).toDomain(updatedRateEntity);
                verify(mapper).toRateBaseResponse(updatedRateDomain);
        }

        @Test
        void execute_shouldThrowException_whenNoCurrentRateFound() {
                // Given
                when(rateRepository.findCurrentRate()).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> useCase.execute(request))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("No active base rate found to update");

                verify(rateRepository, times(1)).findCurrentRate();
                verify(rateRepository, never()).save(any());
        }
}
