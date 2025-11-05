package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.RejectPlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.InvalidPlateChangeStatusException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RejectPlateChangeRequestUseCaseTest {

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

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private com.ayd.parkcontrol.application.port.notification.EmailService emailService;

    @Mock
    private com.ayd.parkcontrol.domain.repository.SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private RejectPlateChangeRequestUseCase rejectPlateChangeRequestUseCase;

    private RejectPlateChangeRequest rejectRequest;
    private PlateChangeRequest mockPlateChangeRequest;
    private User mockReviewer;
    private PlateChangeRequestResponse mockResponse;

    @BeforeEach
    void setUp() {
        rejectRequest = RejectPlateChangeRequest.builder()
                .review_notes("Documentación insuficiente")
                .build();

        mockPlateChangeRequest = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .statusId(1)
                .build();

        mockReviewer = User.builder()
                .id(2L)
                .email("backoffice@parkcontrol.com")
                .firstName("Carlos")
                .lastName("Rodríguez")
                .build();

        mockResponse = PlateChangeRequestResponse.builder()
                .id(1L)
                .user_id(1L)
                .user_name("Juan Pérez")
                .subscription_id(1L)
                .old_license_plate("P-123456")
                .new_license_plate("P-654321")
                .reason_id(1)
                .reason_name("Venta de vehículo")
                .status_code("REJECTED")
                .status_name("Rechazado")
                .reviewed_by(2L)
                .reviewer_name("Carlos Rodríguez")
                .review_notes("Documentación insuficiente")
                .evidence_count(2L)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("backoffice@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldRejectPlateChangeRequestSuccessfully() {
        // Given
        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        ChangeRequestStatusEntity rejectedStatus = new ChangeRequestStatusEntity();
        rejectedStatus.setId(3);
        rejectedStatus.setCode("REJECTED");
        rejectedStatus.setName("Rechazado");

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(1);
        reason.setName("Venta de vehículo");

        User requestUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();

        com.ayd.parkcontrol.domain.model.subscription.Subscription mockSubscription = 
            com.ayd.parkcontrol.domain.model.subscription.Subscription.builder()
                .id(1L)
                .licensePlate("P-123456")
                .build();

        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findByEmail("backoffice@parkcontrol.com")).thenReturn(Optional.of(mockReviewer));
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.of(rejectedStatus));
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestUser));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(mockSubscription));
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason));
        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(mockResponse);

        // When
        PlateChangeRequestResponse result = rejectPlateChangeRequestUseCase.execute(1L, rejectRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus_code()).isEqualTo("REJECTED");
        assertThat(result.getReviewer_name()).isEqualTo("Carlos Rodríguez");
        assertThat(result.getReview_notes()).isEqualTo("Documentación insuficiente");

        verify(plateChangeRequestRepository).findById(1L);
        verify(plateChangeRequestRepository).save(argThat(request -> request.getStatusId() == 3 &&
                request.getReviewedBy() == 2L &&
                request.getReviewNotes().equals("Documentación insuficiente")));
        verify(emailService).sendPlateChangeRejectedNotification(
                eq("juan.perez@example.com"),
                eq("Juan Pérez"),
                eq("P-123456"),
                eq("P-654321"),
                eq("Documentación insuficiente")
        );
    }

    @Test
    void shouldThrowExceptionWhenRequestNotFound() {
        // Given
        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> rejectPlateChangeRequestUseCase.execute(1L, rejectRequest))
                .isInstanceOf(PlateChangeRequestNotFoundException.class);

        verify(plateChangeRequestRepository).findById(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNotPending() {
        // Given
        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        approvedStatus.setName("Aprobado");

        mockPlateChangeRequest.setStatusId(2);

        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(statusRepository.findById(2)).thenReturn(Optional.of(approvedStatus));

        // When / Then
        assertThatThrownBy(() -> rejectPlateChangeRequestUseCase.execute(1L, rejectRequest))
                .isInstanceOf(InvalidPlateChangeStatusException.class)
                .hasMessageContaining("PENDIENTE");

        verify(plateChangeRequestRepository).findById(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRejectedStatusNotFound() {
        // Given
        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findByEmail("backoffice@parkcontrol.com")).thenReturn(Optional.of(mockReviewer));
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> rejectPlateChangeRequestUseCase.execute(1L, rejectRequest))
                .isInstanceOf(InvalidPlateChangeStatusException.class)
                .hasMessageContaining("REJECTED");

        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void shouldHandleReviewerNotFound() {
        // Given
        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        ChangeRequestStatusEntity rejectedStatus = new ChangeRequestStatusEntity();
        rejectedStatus.setId(3);
        rejectedStatus.setCode("REJECTED");
        rejectedStatus.setName("Rechazado");

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(1);
        reason.setName("Venta de vehículo");

        User requestUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findByEmail("backoffice@parkcontrol.com")).thenReturn(Optional.empty());
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.of(rejectedStatus));
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestUser));
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason));
        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), isNull(), anyLong()))
                .thenReturn(mockResponse);

        // When
        PlateChangeRequestResponse result = rejectPlateChangeRequestUseCase.execute(1L, rejectRequest);

        // Then
        assertThat(result).isNotNull();
        verify(plateChangeRequestRepository).save(argThat(request -> request.getReviewedBy() == null));
    }

    @Test
    void shouldCompleteRejection_whenEmailNotificationFails() {
        // Given
        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Pendiente");

        ChangeRequestStatusEntity rejectedStatus = new ChangeRequestStatusEntity();
        rejectedStatus.setId(3);
        rejectedStatus.setCode("REJECTED");
        rejectedStatus.setName("Rechazado");

        PlateChangeReasonEntity reason = new PlateChangeReasonEntity();
        reason.setId(1);
        reason.setName("Venta de vehículo");

        User requestUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();

        com.ayd.parkcontrol.domain.model.subscription.Subscription mockSubscription = 
            com.ayd.parkcontrol.domain.model.subscription.Subscription.builder()
                .id(1L)
                .licensePlate("P-123456")
                .build();

        when(plateChangeRequestRepository.findById(1L)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findByEmail("backoffice@parkcontrol.com")).thenReturn(Optional.of(mockReviewer));
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.of(rejectedStatus));
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestUser));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(mockSubscription));
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reason));
        when(evidenceRepository.countByChangeRequestId(1L)).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(mockResponse);

        // Simular fallo en el envío del email
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendPlateChangeRejectedNotification(anyString(), anyString(), anyString(), anyString(), anyString());

        // When
        PlateChangeRequestResponse result = rejectPlateChangeRequestUseCase.execute(1L, rejectRequest);

        // Then - El proceso debe completarse exitosamente a pesar del fallo en la notificación
        assertThat(result).isNotNull();
        assertThat(result.getStatus_code()).isEqualTo("REJECTED");

        verify(plateChangeRequestRepository).save(any(PlateChangeRequest.class));
        verify(emailService).sendPlateChangeRejectedNotification(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
