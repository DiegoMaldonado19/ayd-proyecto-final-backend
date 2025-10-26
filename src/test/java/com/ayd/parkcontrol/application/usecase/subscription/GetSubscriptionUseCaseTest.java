package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
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
class GetSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private GetSubscriptionUseCase useCase;

    private Subscription subscription;
    private SubscriptionResponse response;

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

        response = SubscriptionResponse.builder()
                .id(1L)
                .licensePlate("ABC123")
                .build();
    }

    @Test
    void execute_shouldReturnSubscription_whenExists() {
        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(subscription));
        when(mapper.toSubscriptionResponse(subscription)).thenReturn(response);

        SubscriptionResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(subscriptionRepository).findByIdWithDetails(1L);
        verify(mapper).toSubscriptionResponse(subscription);
    }

    @Test
    void execute_shouldThrowException_whenSubscriptionNotFound() {
        when(subscriptionRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionRepository).findByIdWithDetails(999L);
        verify(mapper, never()).toSubscriptionResponse(any());
    }
}
