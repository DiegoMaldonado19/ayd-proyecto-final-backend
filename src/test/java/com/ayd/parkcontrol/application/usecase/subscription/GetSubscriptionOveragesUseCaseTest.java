package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionUsageDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
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
@DisplayName("GetSubscriptionOveragesUseCase Tests")
class GetSubscriptionOveragesUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionUsageDtoMapper usageMapper;

    @InjectMocks
    private GetSubscriptionOveragesUseCase getSubscriptionOveragesUseCase;

    private Subscription subscription;
    private List<SubscriptionOverage> overageList;
    private List<SubscriptionOverageResponse> responseList;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
                .id(1L)
                .userId(100L)
                .licensePlate("ABC-123")
                .statusTypeId(1)
                .build();

        SubscriptionOverage overage1 = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(1L)
                .ticketId(1001L)
                .folio("T-001")
                .branchName("Centro")
                .overageHours(BigDecimal.valueOf(5.00))
                .chargedAmount(BigDecimal.valueOf(75.00))
                .appliedRate(BigDecimal.valueOf(15.00))
                .chargedAt(LocalDateTime.now().minusDays(2))
                .build();

        SubscriptionOverage overage2 = SubscriptionOverage.builder()
                .id(2L)
                .subscriptionId(1L)
                .ticketId(1002L)
                .folio("T-002")
                .branchName("Norte")
                .overageHours(BigDecimal.valueOf(3.00))
                .chargedAmount(BigDecimal.valueOf(45.00))
                .appliedRate(BigDecimal.valueOf(15.00))
                .chargedAt(LocalDateTime.now().minusDays(1))
                .build();

        overageList = Arrays.asList(overage1, overage2);

        SubscriptionOverageResponse response1 = SubscriptionOverageResponse.builder()
                .overageId(1L)
                .subscriptionId(1L)
                .ticketId(1001L)
                .overageHours(BigDecimal.valueOf(5.00))
                .amountCharged(BigDecimal.valueOf(75.00))
                .build();

        SubscriptionOverageResponse response2 = SubscriptionOverageResponse.builder()
                .overageId(2L)
                .subscriptionId(1L)
                .ticketId(1002L)
                .overageHours(BigDecimal.valueOf(3.00))
                .amountCharged(BigDecimal.valueOf(45.00))
                .build();

        responseList = Arrays.asList(response1, response2);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should retrieve subscription overages successfully")
    void shouldRetrieveSubscriptionOveragesSuccessfully() {
        Page<SubscriptionOverage> overagePage = new PageImpl<>(overageList, pageable, overageList.size());

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findOveragesBySubscriptionId(eq(1L), any(Pageable.class))).thenReturn(overagePage);
        when(usageMapper.toOverageResponse(any(SubscriptionOverage.class)))
                .thenReturn(responseList.get(0), responseList.get(1));

        Page<SubscriptionOverageResponse> result = getSubscriptionOveragesUseCase.execute(1L, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(subscriptionRepository).findById(1L);
        verify(subscriptionRepository).findOveragesBySubscriptionId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw exception when subscription not found")
    void shouldThrowExceptionWhenSubscriptionNotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> getSubscriptionOveragesUseCase.execute(999L, pageable));

        verify(subscriptionRepository).findById(999L);
        verify(subscriptionRepository, never()).findOveragesBySubscriptionId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no overage records exist")
    void shouldReturnEmptyPageWhenNoOverageRecordsExist() {
        Page<SubscriptionOverage> emptyPage = Page.empty(pageable);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findOveragesBySubscriptionId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        Page<SubscriptionOverageResponse> result = getSubscriptionOveragesUseCase.execute(1L, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
        verify(subscriptionRepository).findOveragesBySubscriptionId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        Pageable customPageable = PageRequest.of(0, 1);
        Page<SubscriptionOverage> overagePage = new PageImpl<>(overageList.subList(0, 1), customPageable,
                overageList.size());

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findOveragesBySubscriptionId(eq(1L), eq(customPageable))).thenReturn(overagePage);
        when(usageMapper.toOverageResponse(any(SubscriptionOverage.class))).thenReturn(responseList.get(0));

        Page<SubscriptionOverageResponse> result = getSubscriptionOveragesUseCase.execute(1L, customPageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getSize());
        assertEquals(2, result.getTotalPages());
        verify(subscriptionRepository).findOveragesBySubscriptionId(eq(1L), eq(customPageable));
    }

    @Test
    @DisplayName("Should retrieve overages with correct amounts")
    void shouldRetrieveOveragesWithCorrectAmounts() {
        Page<SubscriptionOverage> overagePage = new PageImpl<>(overageList, pageable, overageList.size());

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.findOveragesBySubscriptionId(eq(1L), any(Pageable.class))).thenReturn(overagePage);
        when(usageMapper.toOverageResponse(any(SubscriptionOverage.class)))
                .thenReturn(responseList.get(0), responseList.get(1));

        Page<SubscriptionOverageResponse> result = getSubscriptionOveragesUseCase.execute(1L, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(usageMapper, times(2)).toOverageResponse(any(SubscriptionOverage.class));
    }
}
