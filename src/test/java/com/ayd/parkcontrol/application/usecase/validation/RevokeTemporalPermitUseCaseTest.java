package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeTemporalPermitUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaTemporalPermitStatusTypeRepository statusRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private RevokeTemporalPermitUseCase useCase;

    @Test
    void execute_ShouldRevokePermit_WhenActive() {
        TemporalPermitStatusTypeEntity activeStatus = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activo")
                .build();

        TemporalPermitStatusTypeEntity revokedStatus = TemporalPermitStatusTypeEntity.builder()
                .id(3)
                .code("REVOKED")
                .name("Revocado")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .statusTypeId(1)
                .status(activeStatus)
                .approvedBy(5L)
                .build();

        UserEntity approver = UserEntity.builder().id(5L).firstName("Admin").lastName("User").build();

        TemporalPermitResponse expectedResponse = TemporalPermitResponse.builder()
                .id(1L)
                .status("Revocado")
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));
        when(statusRepository.findByCode("REVOKED")).thenReturn(Optional.of(revokedStatus));
        when(temporalPermitRepository.save(any(TemporalPermitEntity.class))).thenReturn(permit);
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(mapper.toResponse(any(TemporalPermitEntity.class), any(UserEntity.class))).thenReturn(expectedResponse);

        TemporalPermitResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("Revocado");
        verify(temporalPermitRepository).save(any(TemporalPermitEntity.class));
    }

    @Test
    void execute_ShouldThrowException_WhenPermitNotFound() {
        when(temporalPermitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Temporal permit not found");
    }

    @Test
    void execute_ShouldThrowException_WhenPermitNotActive() {
        TemporalPermitStatusTypeEntity revokedStatus = TemporalPermitStatusTypeEntity.builder()
                .id(3)
                .code("REVOKED")
                .name("Revocado")
                .build();

        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .statusTypeId(3)
                .status(revokedStatus)
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only active permits can be revoked");
    }
}
