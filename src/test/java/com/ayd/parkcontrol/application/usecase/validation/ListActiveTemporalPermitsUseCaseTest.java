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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListActiveTemporalPermitsUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private ListActiveTemporalPermitsUseCase useCase;

    @Test
    void execute_ShouldReturnActivePermits() {
        TemporalPermitEntity activePermit1 = TemporalPermitEntity.builder()
                .id(1L)
                .temporalPlate("ABC123")
                .approvedBy(5L)
                .build();

        TemporalPermitEntity activePermit2 = TemporalPermitEntity.builder()
                .id(2L)
                .temporalPlate("XYZ789")
                .approvedBy(6L)
                .build();

        UserEntity approver1 = UserEntity.builder().id(5L).build();
        UserEntity approver2 = UserEntity.builder().id(6L).build();

        TemporalPermitResponse response1 = TemporalPermitResponse.builder().id(1L).build();
        TemporalPermitResponse response2 = TemporalPermitResponse.builder().id(2L).build();

        when(temporalPermitRepository.findActivePermits(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(activePermit1, activePermit2));
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver1));
        when(userRepository.findById(6L)).thenReturn(Optional.of(approver2));
        when(mapper.toResponse(activePermit1, approver1)).thenReturn(response1);
        when(mapper.toResponse(activePermit2, approver2)).thenReturn(response2);

        List<TemporalPermitResponse> result = useCase.execute();

        assertThat(result).hasSize(2);
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoActivePermits() {
        when(temporalPermitRepository.findActivePermits(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        List<TemporalPermitResponse> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}
