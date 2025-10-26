package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListSubscriptionsUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private ListSubscriptionsUseCase useCase;

    private Subscription subscription1;
    private Subscription subscription2;
    private SubscriptionResponse response1;
    private SubscriptionResponse response2;

    @BeforeEach
    void setUp() {
        subscription1 = Subscription.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .statusTypeId(1)
                .build();

        subscription2 = Subscription.builder()
                .id(2L)
                .userId(1L)
                .planId(2L)
                .licensePlate("XYZ789")
                .frozenRateBase(new BigDecimal("12.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(1))
                .statusTypeId(1)
                .build();

        response1 = SubscriptionResponse.builder()
                .id(1L)
                .licensePlate("ABC123")
                .build();

        response2 = SubscriptionResponse.builder()
                .id(2L)
                .licensePlate("XYZ789")
                .build();
    }

    @Test
    void execute_shouldReturnAllSubscriptions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subscription> page = new PageImpl<>(Arrays.asList(subscription1, subscription2));
        when(subscriptionRepository.findAllWithDetails(pageable)).thenReturn(page);
        when(mapper.toSubscriptionResponse(any())).thenReturn(response1, response2);

        Page<SubscriptionResponse> result = useCase.execute(pageable);

        assertThat(result).hasSize(2);
        verify(subscriptionRepository).findAllWithDetails(pageable);
    }
}
