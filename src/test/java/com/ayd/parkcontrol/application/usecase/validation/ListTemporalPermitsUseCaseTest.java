package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTemporalPermitsUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private ListTemporalPermitsUseCase useCase;

    @Test
    void execute_ShouldReturnAllPermits() {
        TemporalPermitEntity permit1 = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .approvedBy(5L)
                .build();

        TemporalPermitEntity permit2 = TemporalPermitEntity.builder()
                .id(2L)
                .subscriptionId(200L)
                .temporalPlate("XYZ789")
                .approvedBy(6L)
                .build();

        UserEntity approver1 = UserEntity.builder().id(5L).firstName("Admin").lastName("One").build();
        UserEntity approver2 = UserEntity.builder().id(6L).firstName("Admin").lastName("Two").build();

        TemporalPermitResponse response1 = TemporalPermitResponse.builder().id(1L).temporalPlate("ABC123").build();
        TemporalPermitResponse response2 = TemporalPermitResponse.builder().id(2L).temporalPlate("XYZ789").build();

        when(temporalPermitRepository.findAll()).thenReturn(Arrays.asList(permit1, permit2));
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver1));
        when(userRepository.findById(6L)).thenReturn(Optional.of(approver2));
        when(mapper.toResponse(permit1, approver1)).thenReturn(response1);
        when(mapper.toResponse(permit2, approver2)).thenReturn(response2);

        List<TemporalPermitResponse> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoPermits() {
        when(temporalPermitRepository.findAll()).thenReturn(Arrays.asList());

        List<TemporalPermitResponse> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}
