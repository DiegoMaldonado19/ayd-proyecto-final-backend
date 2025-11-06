package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMySubscriptionBalanceUseCase Tests")
class GetMySubscriptionBalanceUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper subscriptionMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GetMySubscriptionBalanceUseCase getMySubscriptionBalanceUseCase;

    private User mockUser;
    private CustomUserDetails mockUserDetails;
    private Subscription activeSubscription;
    private SubscriptionBalanceResponse balanceResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(100L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        mockUserDetails = new CustomUserDetails(mockUser, "Cliente");

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeName("Full Access")
                .monthlyHours(200)
                .monthlyDiscountPercentage(BigDecimal.valueOf(10.00))
                .build();

        activeSubscription = Subscription.builder()
                .id(1L)
                .userId(100L)
                .planId(1L)
                .plan(plan)
                .licensePlate("ABC-123")
                .frozenRateBase(BigDecimal.valueOf(15.00))
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .consumedHours(BigDecimal.valueOf(50.00))
                .statusTypeId(1)
                .statusName("Activa")
                .isAnnual(false)
                .build();

        balanceResponse = SubscriptionBalanceResponse.builder()
                .subscriptionId(1L)
                .monthlyHours(200)
                .consumedHours(BigDecimal.valueOf(50.00))
                .remainingHours(BigDecimal.valueOf(150.00))
                .consumptionPercentage(BigDecimal.valueOf(25.00))
                .daysUntilExpiration(20L)
                .build();
    }

    @Test
    @DisplayName("Should retrieve subscription balance for authenticated user")
    void shouldRetrieveSubscriptionBalanceForAuthenticatedUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUserDetails);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toBalanceResponse(activeSubscription)).thenReturn(balanceResponse);

            SubscriptionBalanceResponse result = getMySubscriptionBalanceUseCase.execute();

            assertNotNull(result);
            assertEquals(1L, result.getSubscriptionId());
            assertEquals(200, result.getMonthlyHours());
            assertEquals(BigDecimal.valueOf(50.00), result.getConsumedHours());
            assertEquals(BigDecimal.valueOf(150.00), result.getRemainingHours());
            verify(subscriptionRepository).findActiveByUserId(100L);
            verify(subscriptionMapper).toBalanceResponse(activeSubscription);
        }
    }

    @Test
    @DisplayName("Should throw exception when user has no active subscription")
    void shouldThrowExceptionWhenUserHasNoActiveSubscription() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUserDetails);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.empty());

            assertThrows(BusinessRuleException.class, () -> getMySubscriptionBalanceUseCase.execute());

            verify(subscriptionRepository).findActiveByUserId(100L);
            verify(subscriptionMapper, never()).toBalanceResponse(any(Subscription.class));
        }
    }

    @Test
    @DisplayName("Should calculate correct consumption percentage")
    void shouldCalculateCorrectConsumptionPercentage() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUserDetails);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toBalanceResponse(activeSubscription)).thenReturn(balanceResponse);

            SubscriptionBalanceResponse result = getMySubscriptionBalanceUseCase.execute();

            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(25.00), result.getConsumptionPercentage());
        }
    }

    @Test
    @DisplayName("Should use correct user ID from security context")
    void shouldUseCorrectUserIdFromSecurityContext() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            User differentUser = User.builder()
                    .id(999L)
                    .email("different@example.com")
                    .build();
            CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser, "Cliente");

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(differentUserDetails);
            when(subscriptionRepository.findActiveByUserId(999L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toBalanceResponse(activeSubscription)).thenReturn(balanceResponse);

            getMySubscriptionBalanceUseCase.execute();

            verify(subscriptionRepository).findActiveByUserId(999L);
            verify(subscriptionRepository, never()).findActiveByUserId(100L);
        }
    }

    @Test
    @DisplayName("Should calculate remaining days until expiration")
    void shouldCalculateRemainingDaysUntilExpiration() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUserDetails);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toBalanceResponse(activeSubscription)).thenReturn(balanceResponse);

            SubscriptionBalanceResponse result = getMySubscriptionBalanceUseCase.execute();

            assertNotNull(result);
            assertEquals(20L, result.getDaysUntilExpiration());
        }
    }
}
