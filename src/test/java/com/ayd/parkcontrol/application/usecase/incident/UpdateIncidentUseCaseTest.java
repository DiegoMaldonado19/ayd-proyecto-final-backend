package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.UpdateIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateIncidentUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

    @Mock
    private JpaIncidentTypeRepository incidentTypeRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaIncidentEvidenceRepository evidenceRepository;

    @Mock
    private IncidentMapper mapper;

    @InjectMocks
    private UpdateIncidentUseCase updateIncidentUseCase;

    private IncidentEntity incident;
    private IncidentTypeEntity incidentType;
    private BranchEntity branch;
    private UserEntity reportedByUser;
    private UpdateIncidentRequest request;
    private IncidentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(100L)
                .incidentTypeId(1)
                .branchId(1L)
                .licensePlate("ABC123")
                .description("Initial description")
                .isResolved(false)
                .reportedByUserId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        incidentType = IncidentTypeEntity.builder()
                .id(1)
                .code("LOST_TICKET")
                .name("Lost Ticket")
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Centro")
                .build();

        reportedByUser = UserEntity.builder()
                .id(1L)
                .email("operator@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        request = UpdateIncidentRequest.builder()
                .description("Updated description")
                .resolutionNotes("Updated resolution notes")
                .build();

        expectedResponse = IncidentResponse.builder()
                .id(1L)
                .description("Updated description")
                .resolutionNotes("Updated resolution notes")
                .build();
    }

    @Test
    void execute_WithValidData_ShouldUpdateIncident() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(reportedByUser));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(2L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = updateIncidentUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Updated description");
        
        verify(incidentRepository).findById(1L);
        verify(incidentRepository).save(any(IncidentEntity.class));
        verify(mapper).toResponseWithDetails(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    void execute_WithOnlyDescription_ShouldUpdateOnlyDescription() {
        // Arrange
        UpdateIncidentRequest partialRequest = UpdateIncidentRequest.builder()
                .description("Only description updated")
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(reportedByUser));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = updateIncidentUseCase.execute(1L, partialRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(incidentRepository).save(any(IncidentEntity.class));
    }

    @Test
    void execute_WithOnlyResolutionNotes_ShouldUpdateOnlyResolutionNotes() {
        // Arrange
        UpdateIncidentRequest partialRequest = UpdateIncidentRequest.builder()
                .resolutionNotes("Only resolution notes updated")
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(reportedByUser));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(1L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = updateIncidentUseCase.execute(1L, partialRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(incidentRepository).save(any(IncidentEntity.class));
    }

    @Test
    void execute_WithNonExistentIncident_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateIncidentUseCase.execute(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found with ID: 999");

        verify(incidentRepository).findById(999L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_WithResolvedIncident_ShouldLoadResolvedByUser() {
        // Arrange
        UserEntity resolvedByUser = UserEntity.builder()
                .id(2L)
                .email("resolver@parkcontrol.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        incident.setResolvedByUserId(2L);
        incident.setResolvedAt(LocalDateTime.now());

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(reportedByUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(resolvedByUser));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(3L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = updateIncidentUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(2L);
    }

    @Test
    void execute_WithMissingOptionalEntities_ShouldHandleGracefully() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = updateIncidentUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(mapper).toResponseWithDetails(any(), isNull(), isNull(), isNull(), isNull(), eq(0L));
    }
}
