package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestStatusEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeReasonEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeReasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPlateChangeRequestsUseCaseTest {

    @Mock
    private PlateChangeRequestRepository plateChangeRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChangeRequestEvidenceRepository evidenceRepository;

    @Mock
    private JpaPlateChangeReasonRepository reasonRepository;

    @Mock
    private JpaChangeRequestStatusRepository statusRepository;

    @Mock
    private PlateChangeRequestDtoMapper mapper;

    @InjectMocks
    private ListPlateChangeRequestsUseCase listPlateChangeRequestsUseCase;

    @BeforeEach
    void setUp() {
    }

    @Test
    void execute_shouldReturnAllPlateChangeRequests() {
        PlateChangeRequest request1 = PlateChangeRequest.builder()
                .id(1L)
                .userId(1L)
                .reasonId(1)
                .statusId(1)
                .build();

        PlateChangeRequest request2 = PlateChangeRequest.builder()
                .id(2L)
                .userId(2L)
                .reasonId(2)
                .statusId(2)
                .build();

        when(plateChangeRequestRepository.findAll()).thenReturn(Arrays.asList(request1, request2));

        User user1 = User.builder().firstName("Juan").lastName("Pérez").build();
        User user2 = User.builder().firstName("María").lastName("García").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        PlateChangeReasonEntity reason1 = new PlateChangeReasonEntity();
        reason1.setName("Robo");
        PlateChangeReasonEntity reason2 = new PlateChangeReasonEntity();
        reason2.setName("Venta");
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason1));
        when(reasonRepository.findById(2)).thenReturn(Optional.of(reason2));

        ChangeRequestStatusEntity status1 = new ChangeRequestStatusEntity();
        status1.setCode("PENDING");
        status1.setName("Pendiente");
        ChangeRequestStatusEntity status2 = new ChangeRequestStatusEntity();
        status2.setCode("APPROVED");
        status2.setName("Aprobado");
        when(statusRepository.findById(1)).thenReturn(Optional.of(status1));
        when(statusRepository.findById(2)).thenReturn(Optional.of(status2));

        when(evidenceRepository.countByChangeRequestId(anyLong())).thenReturn(0L);

        PlateChangeRequestResponse response1 = PlateChangeRequestResponse.builder()
                .id(1L)
                .user_name("Juan Pérez")
                .build();
        PlateChangeRequestResponse response2 = PlateChangeRequestResponse.builder()
                .id(2L)
                .user_name("María García")
                .build();

        when(mapper.toResponse(eq(request1), anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
                .thenReturn(response1);
        when(mapper.toResponse(eq(request2), anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
                .thenReturn(response2);

        List<PlateChangeRequestResponse> result = listPlateChangeRequestsUseCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(plateChangeRequestRepository).findAll();
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoRequests() {
        when(plateChangeRequestRepository.findAll()).thenReturn(List.of());

        List<PlateChangeRequestResponse> result = listPlateChangeRequestsUseCase.execute();

        assertThat(result).isEmpty();
        verify(plateChangeRequestRepository).findAll();
    }
}
