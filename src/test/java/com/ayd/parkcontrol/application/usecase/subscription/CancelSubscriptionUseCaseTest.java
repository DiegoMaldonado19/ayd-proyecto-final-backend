package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionStatusTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private JpaSubscriptionStatusTypeRepository statusTypeRepository;

    @InjectMocks
    private CancelSubscriptionUseCase useCase;

    private Subscription subscription;
    private SubscriptionStatusTypeEntity statusType;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .statusTypeId(1)
                .build();

        statusType = SubscriptionStatusTypeEntity.builder()
                .id(3)
                .code("CANCELLED")
                .name("Cancelada")
                .build();
    }

    @Test
    void execute_shouldCancelSubscription_whenExists() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(statusTypeRepository.findByCode("CANCELLED")).thenReturn(Optional.of(statusType));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            // Verify status changed to CANCELLED (statusTypeId = 3)
            assert saved.getStatusTypeId() == 3;
            return saved;
        });

        useCase.execute(1L);

        verify(subscriptionRepository).findById(1L);
        verify(statusTypeRepository).findByCode("CANCELLED");
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void execute_shouldThrowException_whenSubscriptionNotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionRepository).findById(999L);
        verify(subscriptionRepository, never()).save(any());
    }
}
