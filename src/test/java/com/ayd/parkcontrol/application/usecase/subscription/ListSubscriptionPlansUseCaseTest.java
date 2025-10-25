package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListSubscriptionPlansUseCaseTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private ListSubscriptionPlansUseCase useCase;

    private SubscriptionPlan plan1;
    private SubscriptionPlan plan2;
    private SubscriptionPlanResponse response1;
    private SubscriptionPlanResponse response2;

    @BeforeEach
    void setUp() {
        plan1 = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .isActive(true)
                .build();

        plan2 = SubscriptionPlan.builder()
                .id(2L)
                .planTypeId(2)
                .monthlyHours(160)
                .monthlyDiscountPercentage(new BigDecimal("15.00"))
                .isActive(true)
                .build();

        response1 = SubscriptionPlanResponse.builder()
                .id(1L)
                .monthlyHours(224)
                .build();

        response2 = SubscriptionPlanResponse.builder()
                .id(2L)
                .monthlyHours(160)
                .build();
    }

    @Test
    void execute_shouldReturnAllActivePlans() {
        List<SubscriptionPlan> plans = Arrays.asList(plan1, plan2);
        when(subscriptionPlanRepository.findAllActiveOrderedByHierarchy()).thenReturn(plans);
        when(mapper.toSubscriptionPlanResponse(plan1)).thenReturn(response1);
        when(mapper.toSubscriptionPlanResponse(plan2)).thenReturn(response2);

        List<SubscriptionPlanResponse> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMonthlyHours()).isEqualTo(224);
        assertThat(result.get(1).getMonthlyHours()).isEqualTo(160);

        verify(subscriptionPlanRepository).findAllActiveOrderedByHierarchy();
        verify(mapper, times(2)).toSubscriptionPlanResponse(any());
    }

    @Test
    void execute_shouldReturnPaginatedPlans_whenIsActiveProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SubscriptionPlan> page = new PageImpl<>(Arrays.asList(plan1, plan2));
        when(subscriptionPlanRepository.findByIsActive(true, pageable)).thenReturn(page);
        when(mapper.toSubscriptionPlanResponse(any())).thenReturn(response1, response2);

        Page<SubscriptionPlanResponse> result = useCase.execute(true, pageable);

        assertThat(result).hasSize(2);
        verify(subscriptionPlanRepository).findByIsActive(true, pageable);
    }
}
