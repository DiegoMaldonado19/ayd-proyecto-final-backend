package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.UpdateSubscriptionPlanRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSubscriptionPlanUseCaseTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private UpdateSubscriptionPlanUseCase useCase;

    private UpdateSubscriptionPlanRequest request;
    private SubscriptionPlan existingPlan;
    private SubscriptionPlanResponse response;

    @BeforeEach
    void setUp() {
        request = UpdateSubscriptionPlanRequest.builder()
                .monthlyHours(160)
                .monthlyDiscountPercentage(new BigDecimal("15.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("6.00"))
                .description("Updated description")
                .isActive(true)
                .build();

        existingPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Old description")
                .isActive(true)
                .build();

        response = SubscriptionPlanResponse.builder()
                .id(1L)
                .monthlyHours(160)
                .monthlyDiscountPercentage(new BigDecimal("15.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("6.00"))
                .description("Updated description")
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldUpdateSubscriptionPlan_whenValidRequest() {
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(existingPlan));
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(existingPlan);
        when(mapper.toSubscriptionPlanResponse(existingPlan)).thenReturn(response);

        SubscriptionPlanResponse result = useCase.execute(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getMonthlyHours()).isEqualTo(160);
        assertThat(result.getDescription()).isEqualTo("Updated description");

        verify(subscriptionPlanRepository).findById(1L);
        verify(subscriptionPlanRepository).save(existingPlan);
        verify(mapper).toSubscriptionPlanResponse(existingPlan);
    }

    @Test
    void execute_shouldThrowException_whenPlanNotFound() {
        when(subscriptionPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, request))
                .isInstanceOf(SubscriptionPlanNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionPlanRepository).findById(999L);
        verify(subscriptionPlanRepository, never()).save(any());
    }

    @Test
    void execute_shouldUpdateIsActive_whenProvided() {
        request.setIsActive(false);
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(existingPlan));
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenAnswer(invocation -> {
            SubscriptionPlan saved = invocation.getArgument(0);
            assertThat(saved.getIsActive()).isFalse();
            return saved;
        });
        when(mapper.toSubscriptionPlanResponse(any())).thenReturn(response);

        useCase.execute(1L, request);

        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }
}
