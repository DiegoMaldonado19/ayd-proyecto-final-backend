package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTemporalPermitUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private GetTemporalPermitUseCase useCase;

    @Test
    void execute_ShouldReturnPermit_WhenExists() {
        TemporalPermitEntity permit = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .approvedBy(5L)
                .build();

        UserEntity approver = UserEntity.builder()
                .id(5L)
                .firstName("Admin")
                .lastName("User")
                .build();

        TemporalPermitResponse expectedResponse = TemporalPermitResponse.builder()
                .id(1L)
                .temporalPlate("ABC123")
                .build();

        when(temporalPermitRepository.findById(1L)).thenReturn(Optional.of(permit));
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(mapper.toResponse(permit, approver)).thenReturn(expectedResponse);

        TemporalPermitResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void execute_ShouldThrowException_WhenPermitNotFound() {
        when(temporalPermitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Temporal permit not found");
    }
}
