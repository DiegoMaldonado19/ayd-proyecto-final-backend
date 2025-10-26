package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPlateChangeRequestUseCaseTest {

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
    private GetPlateChangeRequestUseCase getPlateChangeRequestUseCase;

    private PlateChangeRequest mockPlateChangeRequest;
    private PlateChangeRequestResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockPlateChangeRequest = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .statusId(1)
                .build();

        mockResponse = PlateChangeRequestResponse.builder()
                .id(1L)
                .subscription_id(1L)
                .user_id(1L)
                .user_name("Juan Pérez")
                .old_license_plate("P-123456")
                .new_license_plate("P-654321")
                .reason_id(1)
                .reason_name("Robo")
                .status_id(1)
                .status_code("PENDING")
                .status_name("Pendiente")
                .evidence_count(2L)
                .build();
    }

    @Test
    void execute_shouldReturnPlateChangeRequest_whenExists() {
        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.of(mockPlateChangeRequest));

        User mockUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        PlateChangeReasonEntity reasonEntity = new PlateChangeReasonEntity();
        reasonEntity.setName("Robo");
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reasonEntity));

        ChangeRequestStatusEntity statusEntity = new ChangeRequestStatusEntity();
        statusEntity.setCode("PENDING");
        statusEntity.setName("Pendiente");
        when(statusRepository.findById(1)).thenReturn(Optional.of(statusEntity));

        when(evidenceRepository.countByChangeRequestId(anyLong())).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
                .thenReturn(mockResponse);

        PlateChangeRequestResponse result = getPlateChangeRequestUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser_name()).isEqualTo("Juan Pérez");
        assertThat(result.getEvidence_count()).isEqualTo(2L);

        verify(plateChangeRequestRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(evidenceRepository).countByChangeRequestId(1L);
    }

    @Test
    void execute_shouldThrowPlateChangeRequestNotFoundException_whenDoesNotExist() {
        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPlateChangeRequestUseCase.execute(1L))
                .isInstanceOf(PlateChangeRequestNotFoundException.class);

        verify(plateChangeRequestRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }
}
