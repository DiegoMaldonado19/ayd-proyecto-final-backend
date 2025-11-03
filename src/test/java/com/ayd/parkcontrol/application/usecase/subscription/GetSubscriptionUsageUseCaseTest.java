package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionUsageDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSubscriptionUsageUseCase Tests")
class GetSubscriptionUsageUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionUsageDtoMapper usageMapper;

    @InjectMocks
    private GetSubscriptionUsageUseCase getSubscriptionUsageUseCase;

    private Subscription subscription;
    private List<SubscriptionUsage> usageList;
    private List<SubscriptionUsageResponse> responseList;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
                .id(1L)
                .userId(100L)
                .licensePlate("ABC-123")
                .statusTypeId(1)
                .build();

        SubscriptionUsage usage1 = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(1L)
                .ticketId(1001L)
                .folio("T-001")
                .branchName("Centro")
                .entryTime(LocalDateTime.now().minusHours(5))
                .exitTime(LocalDateTime.now().minusHours(2))
                .hoursConsumed(BigDecimal.valueOf(3.00))
                .build();

        SubscriptionUsage usage2 = SubscriptionUsage.builder()
                .id(2L)
                .subscriptionId(1L)
                .ticketId(1002L)
                .folio("T-002")
                .branchName("Norte")
                .entryTime(LocalDateTime.now().minusHours(10))
                .exitTime(LocalDateTime.now().minusHours(8))
                .hoursConsumed(BigDecimal.valueOf(2.00))
                .build();

        usageList = Arrays.asList(usage1, usage2);

        SubscriptionUsageResponse response1 = SubscriptionUsageResponse.builder()
                .subscriptionId(1L)
                .ticketId(1001L)
                .folio("T-001")
                .hoursConsumed(BigDecimal.valueOf(3.00))
                .build();

        SubscriptionUsageResponse response2 = SubscriptionUsageResponse.builder()
                .subscriptionId(1L)
                .ticketId(1002L)
                .folio("T-002")
                .hoursConsumed(BigDecimal.valueOf(2.00))
                .build();

        responseList = Arrays.asList(response1, response2);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should retrieve subscription usage successfully")
    void shouldRetrieveSubscriptionUsageSuccessfully() {
        Page<SubscriptionUsage> usagePage = new PageImpl<>(usageList, pageable, usageList.size());

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findUsageBySubscriptionId(eq(1L), any(Pageable.class))).thenReturn(usagePage);
        when(usageMapper.toResponse(any(SubscriptionUsage.class))).thenReturn(responseList.get(0), responseList.get(1));

        Page<SubscriptionUsageResponse> result = getSubscriptionUsageUseCase.execute(1L, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(subscriptionRepository).findById(1L);
        verify(subscriptionRepository).findUsageBySubscriptionId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw exception when subscription not found")
    void shouldThrowExceptionWhenSubscriptionNotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> getSubscriptionUsageUseCase.execute(999L, pageable));

        verify(subscriptionRepository).findById(999L);
        verify(subscriptionRepository, never()).findUsageBySubscriptionId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no usage records exist")
    void shouldReturnEmptyPageWhenNoUsageRecordsExist() {
        Page<SubscriptionUsage> emptyPage = Page.empty(pageable);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findUsageBySubscriptionId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        Page<SubscriptionUsageResponse> result = getSubscriptionUsageUseCase.execute(1L, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
        verify(subscriptionRepository).findUsageBySubscriptionId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        Pageable customPageable = PageRequest.of(0, 1);
        Page<SubscriptionUsage> usagePage = new PageImpl<>(usageList.subList(0, 1), customPageable, usageList.size());

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findUsageBySubscriptionId(eq(1L), eq(customPageable))).thenReturn(usagePage);
        when(usageMapper.toResponse(any(SubscriptionUsage.class))).thenReturn(responseList.get(0));

        Page<SubscriptionUsageResponse> result = getSubscriptionUsageUseCase.execute(1L, customPageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getSize());
        assertEquals(2, result.getTotalPages());
        verify(subscriptionRepository).findUsageBySubscriptionId(eq(1L), eq(customPageable));
    }
}
