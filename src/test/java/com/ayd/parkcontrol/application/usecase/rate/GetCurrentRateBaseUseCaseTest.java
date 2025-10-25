package com.ayd.parkcontrol.application.usecase.rate;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentRateBaseUseCaseTest {

    @Mock
    private JpaRateBaseHistoryRepository rateRepository;

    @Mock
    private RateDtoMapper mapper;

    @InjectMocks
    private GetCurrentRateBaseUseCase useCase;

    private RateBaseHistoryEntity testEntity;
    private RateBase testDomain;
    private RateBaseResponse testResponse;

    @BeforeEach
    void setUp() {
        testEntity = RateBaseHistoryEntity.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        testDomain = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        testResponse = RateBaseResponse.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_shouldReturnCurrentRate_whenRateExists() {
        when(rateRepository.findCurrentRate()).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);
        when(mapper.toRateBaseResponse(testDomain)).thenReturn(testResponse);

        RateBaseResponse result = useCase.execute();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getIsActive()).isTrue();

        verify(rateRepository).findCurrentRate();
        verify(mapper).toDomain(testEntity);
        verify(mapper).toRateBaseResponse(testDomain);
    }

    @Test
    void execute_shouldThrowException_whenNoCurrentRateExists() {
        when(rateRepository.findCurrentRate()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No current rate found");

        verify(rateRepository).findCurrentRate();
        verifyNoInteractions(mapper);
    }
}
