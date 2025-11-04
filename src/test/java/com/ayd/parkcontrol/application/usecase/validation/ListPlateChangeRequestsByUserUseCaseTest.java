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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPlateChangeRequestsByUserUseCaseTest {

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
    private ListPlateChangeRequestsByUserUseCase listPlateChangeRequestsByUserUseCase;

    private PlateChangeRequest mockRequest1;
    private PlateChangeRequest mockRequest2;
    private PlateChangeRequestResponse mockResponse1;
    private PlateChangeRequestResponse mockResponse2;

    @BeforeEach
    void setUp() {
        mockRequest1 = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .statusId(1)
                .reviewedBy(null)
                .build();

        mockRequest2 = PlateChangeRequest.builder()
                .id(2L)
                .subscriptionId(2L)
                .userId(1L)
                .oldLicensePlate("P-789012")
                .newLicensePlate("P-210987")
                .reasonId(2)
                .statusId(2)
                .reviewedBy(2L)
                .build();

        mockResponse1 = PlateChangeRequestResponse.builder()
                .id(1L)
                .user_id(1L)
                .user_name("Juan Pérez")
                .subscription_id(1L)
                .old_license_plate("P-123456")
                .new_license_plate("P-654321")
                .reason_id(1)
                .reason_name("Robo")
                .status_code("PENDING")
                .status_name("Pendiente")
                .evidence_count(2L)
                .build();

        mockResponse2 = PlateChangeRequestResponse.builder()
                .id(2L)
                .user_id(1L)
                .user_name("Juan Pérez")
                .subscription_id(2L)
                .old_license_plate("P-789012")
                .new_license_plate("P-210987")
                .reason_id(2)
                .reason_name("Venta de vehículo")
                .status_code("APPROVED")
                .status_name("Aprobado")
                .reviewed_by(2L)
                .reviewer_name("Carlos Rodríguez")
                .evidence_count(1L)
                .build();
    }

    @Test
    void shouldListPlateChangeRequestsByUserSuccessfully() {
        // Given
        Long userId = 1L;

        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        approvedStatus.setName("Aprobado");

        PlateChangeReasonEntity reason1 = new PlateChangeReasonEntity();
        reason1.setId(1);
        reason1.setName("Robo");

        PlateChangeReasonEntity reason2 = new PlateChangeReasonEntity();
        reason2.setId(2);
        reason2.setName("Venta de vehículo");

        User user = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        User reviewer = User.builder()
                .id(2L)
                .firstName("Carlos")
                .lastName("Rodríguez")
                .build();

        when(plateChangeRequestRepository.findByUserId(userId)).thenReturn(List.of(mockRequest1, mockRequest2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));

        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason1));
        when(reasonRepository.findById(2)).thenReturn(Optional.of(reason2));

        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(statusRepository.findById(2)).thenReturn(Optional.of(approvedStatus));

        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(evidenceRepository.countByChangeRequestId(2L)).thenReturn(1L);

        when(mapper.toResponse(eq(mockRequest1), anyString(), anyString(), anyString(), anyString(), isNull(),
                anyLong()))
                .thenReturn(mockResponse1);
        when(mapper.toResponse(eq(mockRequest2), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyLong()))
                .thenReturn(mockResponse2);

        // When
        List<PlateChangeRequestResponse> results = listPlateChangeRequestsByUserUseCase.execute(userId);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getUser_id()).isEqualTo(1L);
        assertThat(results.get(1).getUser_id()).isEqualTo(1L);

        verify(plateChangeRequestRepository).findByUserId(userId);
        verify(userRepository, times(3)).findById(anyLong()); // user (2 times) + reviewer (1 time)
        verify(reasonRepository, times(2)).findById(anyInt());
        verify(statusRepository, times(2)).findById(anyInt());
        verify(evidenceRepository, times(2)).countByChangeRequestId(anyLong());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRequests() {
        // Given
        Long userId = 999L;
        when(plateChangeRequestRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<PlateChangeRequestResponse> results = listPlateChangeRequestsByUserUseCase.execute(userId);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();

        verify(plateChangeRequestRepository).findByUserId(userId);
        verify(userRepository, never()).findById(anyLong());
        verify(reasonRepository, never()).findById(anyInt());
    }

    @Test
    void shouldHandleUserNotFound() {
        // Given
        Long userId = 1L;

        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(1);
        reason.setName("Robo");

        when(plateChangeRequestRepository.findByUserId(userId)).thenReturn(List.of(mockRequest1));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason));
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(mapper.toResponse(any(), eq("Unknown"), anyString(), anyString(), anyString(), isNull(), anyLong()))
                .thenReturn(mockResponse1);

        // When
        List<PlateChangeRequestResponse> results = listPlateChangeRequestsByUserUseCase.execute(userId);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        verify(mapper).toResponse(any(), eq("Unknown"), anyString(), anyString(), anyString(), isNull(), anyLong());
    }

    @Test
    void shouldHandleStatusNotFound() {
        // Given
        Long userId = 1L;

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(1);
        reason.setName("Robo");

        User user = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(plateChangeRequestRepository.findByUserId(userId)).thenReturn(List.of(mockRequest1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason));
        when(statusRepository.findById(1)).thenReturn(Optional.empty());
        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), eq("UNKNOWN"), eq("Unknown"), isNull(), anyLong()))
                .thenReturn(mockResponse1);

        // When
        List<PlateChangeRequestResponse> results = listPlateChangeRequestsByUserUseCase.execute(userId);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        verify(mapper).toResponse(any(), anyString(), anyString(), eq("UNKNOWN"), eq("Unknown"), isNull(), anyLong());
    }

    @Test
    void shouldHandleReviewerNotFound() {
        // Given
        Long userId = 1L;

        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        approvedStatus.setName("Aprobado");

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(2);
        reason.setName("Venta de vehículo");

        User user = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(plateChangeRequestRepository.findByUserId(userId)).thenReturn(List.of(mockRequest2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(reasonRepository.findById(2)).thenReturn(Optional.of(reason));
        when(statusRepository.findById(2)).thenReturn(Optional.of(approvedStatus));
        when(evidenceRepository.countByChangeRequestId(2L)).thenReturn(1L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), isNull(), anyLong()))
                .thenReturn(mockResponse2);

        // When
        List<PlateChangeRequestResponse> results = listPlateChangeRequestsByUserUseCase.execute(userId);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        verify(mapper).toResponse(any(), anyString(), anyString(), anyString(), anyString(), isNull(), anyLong());
    }
}
