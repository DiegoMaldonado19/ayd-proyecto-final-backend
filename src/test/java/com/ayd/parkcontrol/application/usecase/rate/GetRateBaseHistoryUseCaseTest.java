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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRateBaseHistoryUseCaseTest {

    @Mock
    private JpaRateBaseHistoryRepository rateRepository;

    @Mock
    private RateDtoMapper mapper;

    @InjectMocks
    private GetRateBaseHistoryUseCase useCase;

    private RateBaseHistoryEntity entity1;
    private RateBaseHistoryEntity entity2;
    private RateBase domain1;
    private RateBase domain2;
    private RateBaseResponse response1;
    private RateBaseResponse response2;

    @BeforeEach
    void setUp() {
        entity1 = RateBaseHistoryEntity.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        entity2 = RateBaseHistoryEntity.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("6.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        domain1 = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .build();

        domain2 = RateBase.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("6.00"))
                .build();

        response1 = RateBaseResponse.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .build();

        response2 = RateBaseResponse.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("6.00"))
                .build();
    }

    @Test
    void execute_shouldReturnHistoryOrderedByStartDateDesc() {
        List<RateBaseHistoryEntity> entities = Arrays.asList(entity2, entity1);
        when(rateRepository.findAllOrderByStartDateDesc()).thenReturn(entities);
        when(mapper.toDomain(entity2)).thenReturn(domain2);
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toRateBaseResponse(domain2)).thenReturn(response2);
        when(mapper.toRateBaseResponse(domain1)).thenReturn(response1);

        List<RateBaseResponse> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);

        verify(rateRepository).findAllOrderByStartDateDesc();
        verify(mapper, times(2)).toDomain(any(RateBaseHistoryEntity.class));
        verify(mapper, times(2)).toRateBaseResponse(any(RateBase.class));
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoHistoryExists() {
        when(rateRepository.findAllOrderByStartDateDesc()).thenReturn(List.of());

        List<RateBaseResponse> result = useCase.execute();

        assertThat(result).isEmpty();
        verify(rateRepository).findAllOrderByStartDateDesc();
        verifyNoMoreInteractions(mapper);
    }
}
