package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.ApprovePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.InvalidPlateChangeStatusException;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
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

@ExtendWith(MockitoExtension.class)
class ApprovePlateChangeRequestUseCaseTest {

    @Mock
    private PlateChangeRequestRepository plateChangeRequestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

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

    @InjectMocks
    private ApprovePlateChangeRequestUseCase approvePlateChangeRequestUseCase;

    private ApprovePlateChangeRequest approveRequest;
    private PlateChangeRequest mockPlateChangeRequest;
    private Subscription mockSubscription;
    private User mockReviewer;
    private PlateChangeRequestResponse mockResponse;

    @BeforeEach
    void setUp() {
        approveRequest = ApprovePlateChangeRequest.builder()
                .review_notes("Documentación verificada")
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

        mockSubscription = Subscription.builder()
                .id(1L)
                .licensePlate("P-123456")
                .build();

        mockReviewer = User.builder()
                .id(2L)
                .email("backoffice@parkcontrol.com")
                .firstName("Carlos")
                .lastName("Rodríguez")
                .build();

        mockResponse = PlateChangeRequestResponse.builder()
                .id(1L)
                .status_code("APPROVED")
                .status_name("Aprobado")
                .build();
    }

    @Test
    void execute_shouldApproveRequest_whenValidPendingRequest() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("backoffice@parkcontrol.com");

        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.of(mockPlateChangeRequest));

        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));

        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        approvedStatus.setName("Aprobado");
        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockReviewer));
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(mockSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);

        PlateChangeReasonEntity reasonEntity = new PlateChangeReasonEntity();
        reasonEntity.setName("Robo");
        when(reasonRepository.findById(anyInt())).thenReturn(Optional.of(reasonEntity));

        User requestUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestUser));
        when(evidenceRepository.countByChangeRequestId(anyLong())).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(mockResponse);

        PlateChangeRequestResponse result = approvePlateChangeRequestUseCase.execute(1L, approveRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus_code()).isEqualTo("APPROVED");

        verify(plateChangeRequestRepository).save(any(PlateChangeRequest.class));
        verify(subscriptionRepository).save(argThat(subscription -> subscription.getLicensePlate().equals("P-654321")));
        verify(emailService).sendPlateChangeApprovedNotification(
                eq("juan.perez@example.com"),
                eq("Juan Pérez"),
                eq("P-123456"),
                eq("P-654321"),
                eq("Documentación verificada")
        );
    }

    @Test
    void execute_shouldThrowPlateChangeRequestNotFoundException_whenRequestDoesNotExist() {
        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvePlateChangeRequestUseCase.execute(1L, approveRequest))
                .isInstanceOf(PlateChangeRequestNotFoundException.class);

        verify(plateChangeRequestRepository).findById(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowInvalidPlateChangeStatusException_whenRequestNotPending() {
        mockPlateChangeRequest.setStatusId(2);
        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.of(mockPlateChangeRequest));

        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        when(statusRepository.findById(2)).thenReturn(Optional.of(approvedStatus));

        assertThatThrownBy(() -> approvePlateChangeRequestUseCase.execute(1L, approveRequest))
                .isInstanceOf(InvalidPlateChangeStatusException.class)
                .hasMessageContaining("Solo se pueden aprobar solicitudes con estado PENDIENTE");

        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void execute_shouldCompleteApproval_whenEmailNotificationFails() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("backoffice@parkcontrol.com");

        when(plateChangeRequestRepository.findById(anyLong())).thenReturn(Optional.of(mockPlateChangeRequest));

        ChangeRequestStatusEntity pendingStatus = new ChangeRequestStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setCode("PENDING");
        when(statusRepository.findById(1)).thenReturn(Optional.of(pendingStatus));

        ChangeRequestStatusEntity approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");
        approvedStatus.setName("Aprobado");
        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockReviewer));
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(mockSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);

        PlateChangeReasonEntity reasonEntity = new PlateChangeReasonEntity();
        reasonEntity.setName("Robo");
        when(reasonRepository.findById(anyInt())).thenReturn(Optional.of(reasonEntity));

        User requestUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestUser));
        when(evidenceRepository.countByChangeRequestId(anyLong())).thenReturn(2L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(mockResponse);

        // Simular fallo en el envío del email
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendPlateChangeApprovedNotification(anyString(), anyString(), anyString(), anyString(), anyString());

        PlateChangeRequestResponse result = approvePlateChangeRequestUseCase.execute(1L, approveRequest);

        // El proceso debe completarse exitosamente a pesar del fallo en la notificación
        assertThat(result).isNotNull();
        assertThat(result.getStatus_code()).isEqualTo("APPROVED");

        verify(plateChangeRequestRepository).save(any(PlateChangeRequest.class));
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(emailService).sendPlateChangeApprovedNotification(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
