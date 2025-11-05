package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.UpdateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTemporalPermitUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private UpdateTemporalPermitUseCase useCase;

    @Test
    void execute_ShouldUpdatePermit_WhenValidRequest() {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .maxUses(15)
                .allowedBranches(Arrays.asList(1L, 2L, 3L))
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activo")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .currentUses(5)
                .statusTypeId(1)
                .status(activeStatus)
                .approvedBy(5L)
                .build();

        UserEntity approver = UserEntity.builder().id(5L).firstName("Admin").lastName("User").build();

        TemporalPermitResponse expectedResponse = TemporalPermitResponse.builder()
                .id(1L)
                .maxUses(15)
                .build();

        when(temporalPermitRepository.findById(1L))
                .thenReturn(Optional.of(permit))
                .thenReturn(Optional.of(permit));
        when(mapper.serializeAllowedBranches(any())).thenReturn("[1,2,3]");
        when(temporalPermitRepository.save(any(TemporalPermitEntity.class))).thenReturn(permit);
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(mapper.toResponse(any(TemporalPermitEntity.class), any(UserEntity.class))).thenReturn(expectedResponse);

        TemporalPermitResponse result = useCase.execute(1L, request);

        assertThat(result).isNotNull();
        verify(temporalPermitRepository).save(any(TemporalPermitEntity.class));
    }

    @Test
    void execute_ShouldThrowException_WhenPermitNotFound() {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .maxUses(15)
                .build();

        when(temporalPermitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Temporal permit not found");
    }

    @Test
    void execute_ShouldThrowException_WhenPermitNotActive() {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .maxUses(15)
                .build();

        TemporalPermitStatusTypeEntity revokedStatus = TemporalPermitStatusTypeEntity.builder()
                .id(2)
                .code("REVOKED")
                .name("Revocado")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .statusTypeId(2)
                .status(revokedStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only active permits can be updated");
    }

    @Test
    void execute_ShouldThrowException_WhenMaxUsesLessThanCurrentUses() {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .maxUses(3)
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(5)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .status(activeStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Max uses cannot be less than current uses");
    }

    @Test
    void execute_ShouldThrowException_WhenDurationExceeds30Days() {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(35))
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(0)
                .maxUses(10)
                .status(activeStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot exceed 30 days");
    }

    @Test
    void execute_shouldValidateDateCalculationCorrectly_whenUpdatingDates() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validEndDate = now.plusDays(30); // Exactly 30 days
        
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .startDate(now)
                .endDate(validEndDate)
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(0)
                .maxUses(10)
                .status(activeStatus)
                .approvedBy(1L)
                .build();

        UserEntity approver = UserEntity.builder()
                .id(1L)
                .firstName("Admin")
                .lastName("User")
                .build();

        TemporalPermitResponse expectedResponse = TemporalPermitResponse.builder()
                .id(1L)
                .maxUses(10)
                .currentUses(0)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));
        when(temporalPermitRepository.save(any(TemporalPermitEntity.class))).thenReturn(permit);
        when(userRepository.findById(1L)).thenReturn(Optional.of(approver));
        when(mapper.toResponse(any(TemporalPermitEntity.class), any(UserEntity.class)))
                .thenReturn(expectedResponse);

        // When
        TemporalPermitResponse result = useCase.execute(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(permit.getStartDate()).isEqualTo(now);
        assertThat(permit.getEndDate()).isEqualTo(validEndDate);

        verify(temporalPermitRepository).save(permit);
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenDurationExceeds30Days() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime invalidEndDate = now.plusDays(31); // 31 days - should fail
        
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .startDate(now)
                .endDate(invalidEndDate)
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(0)
                .maxUses(10)
                .status(activeStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Permit duration cannot exceed 30 days. Current duration: 31 days");

        verify(temporalPermitRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenEndDateBeforeStartDate() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime invalidEndDate = now.minusDays(1); // End date before start date
        
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .startDate(now)
                .endDate(invalidEndDate)
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(0)
                .maxUses(10)
                .status(activeStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("End date must be after start date");

        verify(temporalPermitRepository, never()).save(any());
    }

    @Test
    void execute_shouldHandleDatabaseConstraintError_withImprovedMessage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(25);
        
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .startDate(now)
                .endDate(endDate)
                .build();

        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .currentUses(0)
                .maxUses(10)
                .status(activeStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));
        
        // Simulate database constraint violation
        RuntimeException dbException = new RuntimeException("CONSTRAINT 'chk_permit_duration' failed");
        when(temporalPermitRepository.save(any(TemporalPermitEntity.class))).thenThrow(dbException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La duración del permiso no puede exceder 30 días");

        verify(temporalPermitRepository).save(any());
    }
}
