package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
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
@DisplayName("GetMySubscriptionUseCase Tests")
class GetMySubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper subscriptionMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GetMySubscriptionUseCase getMySubscriptionUseCase;

    private User mockUser;
    private Subscription activeSubscription;
    private SubscriptionResponse response;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(100L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

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

        response = SubscriptionResponse.builder()
                .id(1L)
                .userId(100L)
                .licensePlate("ABC-123")
                .statusName("Activa")
                .build();
    }

    @Test
    @DisplayName("Should retrieve active subscription for authenticated user")
    void shouldRetrieveActiveSubscriptionForAuthenticatedUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toSubscriptionResponse(activeSubscription)).thenReturn(response);

            SubscriptionResponse result = getMySubscriptionUseCase.execute();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("ABC-123", result.getLicensePlate());
            verify(subscriptionRepository).findActiveByUserId(100L);
            verify(subscriptionMapper).toSubscriptionResponse(activeSubscription);
        }
    }

    @Test
    @DisplayName("Should throw exception when user has no active subscription")
    void shouldThrowExceptionWhenUserHasNoActiveSubscription() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(
                SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(subscriptionRepository.findActiveByUserId(100L)).thenReturn(Optional.empty());

            assertThrows(BusinessRuleException.class, () -> getMySubscriptionUseCase.execute());

            verify(subscriptionRepository).findActiveByUserId(100L);
            verify(subscriptionMapper, never()).toSubscriptionResponse(any(Subscription.class));
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

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(differentUser);
            when(subscriptionRepository.findActiveByUserId(999L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionMapper.toSubscriptionResponse(activeSubscription)).thenReturn(response);

            getMySubscriptionUseCase.execute();

            verify(subscriptionRepository).findActiveByUserId(999L);
            verify(subscriptionRepository, never()).findActiveByUserId(100L);
        }
    }
}
