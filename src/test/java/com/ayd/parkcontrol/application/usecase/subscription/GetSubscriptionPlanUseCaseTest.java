package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionPlanNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSubscriptionPlanUseCaseTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private GetSubscriptionPlanUseCase useCase;

    private SubscriptionPlan subscriptionPlan;
    private SubscriptionPlanResponse response;

    @BeforeEach
    void setUp() {
        subscriptionPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .isActive(true)
                .build();

        response = SubscriptionPlanResponse.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldReturnSubscriptionPlan_whenExists() {
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(subscriptionPlan));
        when(mapper.toSubscriptionPlanResponse(subscriptionPlan)).thenReturn(response);

        SubscriptionPlanResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(subscriptionPlanRepository).findById(1L);
        verify(mapper).toSubscriptionPlanResponse(subscriptionPlan);
    }

    @Test
    void execute_shouldThrowException_whenPlanNotFound() {
        when(subscriptionPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionPlanNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionPlanRepository).findById(999L);
        verify(mapper, never()).toSubscriptionPlanResponse(any());
    }
}
