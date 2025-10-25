package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.CreateSubscriptionPlanRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicatePlanTypeException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSubscriptionPlanUseCaseTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private CreateSubscriptionPlanUseCase useCase;

    private CreateSubscriptionPlanRequest request;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionPlanResponse response;

    @BeforeEach
    void setUp() {
        request = CreateSubscriptionPlanRequest.builder()
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Full Access Plan")
                .build();

        subscriptionPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Full Access Plan")
                .isActive(true)
                .build();

        response = SubscriptionPlanResponse.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Full Access Plan")
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldCreateSubscriptionPlan_whenValidRequest() {
        when(subscriptionPlanRepository.existsByPlanTypeId(1)).thenReturn(false);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(subscriptionPlan);
        when(mapper.toSubscriptionPlanResponse(subscriptionPlan)).thenReturn(response);

        SubscriptionPlanResponse result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlanTypeId()).isEqualTo(1);
        assertThat(result.getMonthlyHours()).isEqualTo(224);
        assertThat(result.getIsActive()).isTrue();

        verify(subscriptionPlanRepository).existsByPlanTypeId(1);
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
        verify(mapper).toSubscriptionPlanResponse(subscriptionPlan);
    }

    @Test
    void execute_shouldThrowException_whenPlanTypeAlreadyExists() {
        when(subscriptionPlanRepository.existsByPlanTypeId(1)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(DuplicatePlanTypeException.class)
                .hasMessageContaining("already exists for plan type ID: 1");

        verify(subscriptionPlanRepository).existsByPlanTypeId(1);
        verify(subscriptionPlanRepository, never()).save(any());
        verify(mapper, never()).toSubscriptionPlanResponse(any());
    }

    @Test
    void execute_shouldSetIsActiveToTrue_byDefault() {
        when(subscriptionPlanRepository.existsByPlanTypeId(1)).thenReturn(false);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenAnswer(invocation -> {
            SubscriptionPlan saved = invocation.getArgument(0);
            assertThat(saved.getIsActive()).isTrue();
            return saved;
        });
        when(mapper.toSubscriptionPlanResponse(any())).thenReturn(response);

        useCase.execute(request);

        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }
}
