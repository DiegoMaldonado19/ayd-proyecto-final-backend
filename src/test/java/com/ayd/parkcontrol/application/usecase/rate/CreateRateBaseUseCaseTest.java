package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.CreateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRateBaseUseCaseTest {

    @Mock
    private JpaRateBaseHistoryRepository rateRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private RateDtoMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CreateRateBaseUseCase useCase;

    private CreateRateBaseRequest request;
    private UserEntity userEntity;
    private RateBaseHistoryEntity oldRateEntity;
    private RateBaseHistoryEntity newRateEntity;
    private RateBase newRateDomain;
    private RateBaseResponse response;

    @BeforeEach
    void setUp() {
        request = CreateRateBaseRequest.builder()
                .amountPerHour(new BigDecimal("7.50"))
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .email("admin@parkcontrol.com")
                .build();

        oldRateEntity = RateBaseHistoryEntity.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("5.00"))
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(null)
                .isActive(true)
                .build();

        newRateEntity = RateBaseHistoryEntity.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("7.50"))
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isActive(true)
                .createdBy(userEntity)
                .build();

        newRateDomain = RateBase.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("7.50"))
                .startDate(LocalDateTime.now())
                .build();

        response = RateBaseResponse.builder()
                .id(2L)
                .amountPerHour(new BigDecimal("7.50"))
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldCreateNewRateAndDeactivateOldOne() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@parkcontrol.com");
        when(userRepository.findByEmail("admin@parkcontrol.com")).thenReturn(Optional.of(userEntity));
        when(rateRepository.findAllActive()).thenReturn(List.of(oldRateEntity));
        when(rateRepository.save(any(RateBaseHistoryEntity.class))).thenReturn(newRateEntity);
        when(mapper.toDomain(newRateEntity)).thenReturn(newRateDomain);
        when(mapper.toRateBaseResponse(newRateDomain)).thenReturn(response);

        RateBaseResponse result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("7.50"));

        verify(rateRepository, times(2)).save(any(RateBaseHistoryEntity.class));
        verify(mapper).toDomain(newRateEntity);
        verify(mapper).toRateBaseResponse(newRateDomain);
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@parkcontrol.com");
        when(userRepository.findByEmail("unknown@parkcontrol.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(rateRepository, never()).save(any());
    }
}
