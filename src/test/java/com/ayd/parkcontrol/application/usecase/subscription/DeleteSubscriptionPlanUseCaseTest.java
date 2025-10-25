package com.ayd.parkcontrol.application.usecase.subscription;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteSubscriptionPlanUseCaseTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private DeleteSubscriptionPlanUseCase useCase;

    private SubscriptionPlan subscriptionPlan;

    @BeforeEach
    void setUp() {
        subscriptionPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldSoftDeletePlan_whenExists() {
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenAnswer(invocation -> {
            SubscriptionPlan saved = invocation.getArgument(0);
            assert !saved.getIsActive();
            return saved;
        });

        useCase.execute(1L);

        verify(subscriptionPlanRepository).findById(1L);
        verify(subscriptionPlanRepository).save(subscriptionPlan);
    }

    @Test
    void execute_shouldThrowException_whenPlanNotFound() {
        when(subscriptionPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionPlanNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionPlanRepository).findById(999L);
        verify(subscriptionPlanRepository, never()).save(any());
    }
}
