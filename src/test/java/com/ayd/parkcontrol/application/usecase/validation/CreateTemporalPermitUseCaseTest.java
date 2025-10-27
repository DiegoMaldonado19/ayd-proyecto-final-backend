package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTemporalPermitUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaTemporalPermitStatusTypeRepository statusRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private CreateTemporalPermitUseCase useCase;

    @Test
    void execute_ShouldCreateTemporalPermit_WhenValidRequest() {
        // Setup security context for this specific test
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@parkcontrol.com");

        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .allowedBranches(Arrays.asList(1L, 2L))
                .vehicleTypeId(1)
                .build();

        SubscriptionStatusTypeEntity subscriptionStatus = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activa")
                .build();

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .statusTypeId(1)
                .status(subscriptionStatus)
                .build();

        VehicleTypeEntity vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        UserEntity approver = UserEntity.builder()
                .id(5L)
                .email("admin@parkcontrol.com")
                .firstName("Admin")
                .lastName("User")
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activo")
                .build();

        TemporalPermitEntity savedEntity = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxUses(10)
                .currentUses(0)
                .vehicleTypeId(1)
                .statusTypeId(1)
                .approvedBy(5L)
                .build();

        TemporalPermitResponse expectedResponse = TemporalPermitResponse.builder()
                .id(1L)
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));
        when(userRepository.findByEmail("admin@parkcontrol.com")).thenReturn(Optional.of(approver));
        when(statusRepository.findByCode("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(temporalPermitRepository.hasActivePermitForSubscription(anyLong(), any(LocalDateTime.class)))
                .thenReturn(false);
        when(mapper.serializeAllowedBranches(any())).thenReturn("[1,2]");
        when(temporalPermitRepository.save(any(TemporalPermitEntity.class))).thenReturn(savedEntity);
        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(any(TemporalPermitEntity.class), any(UserEntity.class))).thenReturn(expectedResponse);

        TemporalPermitResponse response = useCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(temporalPermitRepository).save(any(TemporalPermitEntity.class));
    }

    @Test
    void execute_ShouldThrowException_WhenSubscriptionNotFound() {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(999L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    void execute_ShouldThrowException_WhenSubscriptionNotActive() {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        SubscriptionStatusTypeEntity expiredStatus = SubscriptionStatusTypeEntity.builder()
                .id(2)
                .code("EXPIRED")
                .name("Vencida")
                .build();

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .statusTypeId(2)
                .status(expiredStatus)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Subscription must be active");
    }

    @Test
    void execute_ShouldThrowException_WhenEndDateBeforeStartDate() {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now())
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        SubscriptionStatusTypeEntity activeStatus = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .status(activeStatus)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void execute_ShouldThrowException_WhenDurationExceeds30Days() {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(35))
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        SubscriptionStatusTypeEntity activeStatus = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .status(activeStatus)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot exceed 30 days");
    }

    @Test
    void execute_ShouldThrowException_WhenActivePermitAlreadyExists() {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(1L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        SubscriptionStatusTypeEntity activeStatus = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .status(activeStatus)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(temporalPermitRepository.hasActivePermitForSubscription(anyLong(), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has an active temporal permit");
    }
}
