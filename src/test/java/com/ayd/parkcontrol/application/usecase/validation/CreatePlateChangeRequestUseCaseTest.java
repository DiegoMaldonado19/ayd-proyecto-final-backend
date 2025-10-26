package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreatePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.PendingPlateChangeRequestException;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePlateChangeRequestUseCaseTest {

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

    @InjectMocks
    private CreatePlateChangeRequestUseCase createPlateChangeRequestUseCase;

    private CreatePlateChangeRequest validRequest;
    private Subscription mockSubscription;
    private User mockUser;
    private PlateChangeRequest mockPlateChangeRequest;
    private PlateChangeRequest savedPlateChangeRequest;
    private PlateChangeRequestResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = CreatePlateChangeRequest.builder()
                .subscription_id(1L)
                .user_id(1L)
                .old_license_plate("P-123456")
                .new_license_plate("P-654321")
                .reason_id(1)
                .notes("Robo de vehículo")
                .build();

        mockSubscription = Subscription.builder()
                .id(1L)
                .licensePlate("P-123456")
                .build();

        mockUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        mockPlateChangeRequest = PlateChangeRequest.builder()
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .notes("Robo de vehículo")
                .statusId(1)
                .build();

        savedPlateChangeRequest = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .notes("Robo de vehículo")
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
                .evidence_count(0L)
                .build();
    }

    @Test
    void execute_shouldCreatePlateChangeRequest_whenValidRequest() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(mockSubscription));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(plateChangeRequestRepository.hasActivePendingRequest(anyLong())).thenReturn(false);
        when(mapper.toDomain(any(CreatePlateChangeRequest.class))).thenReturn(mockPlateChangeRequest);
        when(plateChangeRequestRepository.save(any(PlateChangeRequest.class))).thenReturn(savedPlateChangeRequest);

        PlateChangeReasonEntity reasonEntity = new PlateChangeReasonEntity();
        reasonEntity.setId(1);
        reasonEntity.setName("Robo");
        when(reasonRepository.findById(1)).thenReturn(Optional.of(reasonEntity));

        ChangeRequestStatusEntity statusEntity = new ChangeRequestStatusEntity();
        statusEntity.setId(1);
        statusEntity.setCode("PENDING");
        statusEntity.setName("Pendiente");
        when(statusRepository.findById(1)).thenReturn(Optional.of(statusEntity));

        when(evidenceRepository.countByChangeRequestId(anyLong())).thenReturn(0L);
        when(mapper.toResponse(any(), anyString(), anyString(), anyString(), anyString(), any(), anyLong()))
                .thenReturn(mockResponse);

        PlateChangeRequestResponse result = createPlateChangeRequestUseCase.execute(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOld_license_plate()).isEqualTo("P-123456");
        assertThat(result.getNew_license_plate()).isEqualTo("P-654321");

        verify(subscriptionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(plateChangeRequestRepository).hasActivePendingRequest(1L);
        verify(plateChangeRequestRepository).save(any(PlateChangeRequest.class));
    }

    @Test
    void execute_shouldThrowSubscriptionNotFoundException_whenSubscriptionDoesNotExist() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createPlateChangeRequestUseCase.execute(validRequest))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("Suscripción no encontrada");

        verify(subscriptionRepository).findById(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(mockSubscription));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createPlateChangeRequestUseCase.execute(validRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(subscriptionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowPendingPlateChangeRequestException_whenPendingRequestExists() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(mockSubscription));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(plateChangeRequestRepository.hasActivePendingRequest(anyLong())).thenReturn(true);

        assertThatThrownBy(() -> createPlateChangeRequestUseCase.execute(validRequest))
                .isInstanceOf(PendingPlateChangeRequestException.class)
                .hasMessageContaining("ya tiene una solicitud de cambio de placa pendiente");

        verify(subscriptionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(plateChangeRequestRepository).hasActivePendingRequest(1L);
        verify(plateChangeRequestRepository, never()).save(any());
    }
}
