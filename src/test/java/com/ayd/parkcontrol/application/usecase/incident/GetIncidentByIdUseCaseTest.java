package com.ayd.parkcontrol.application.usecase.incident;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetIncidentByIdUseCaseTest {

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
    private GetIncidentByIdUseCase getIncidentByIdUseCase;

    private IncidentEntity incident;
    private IncidentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .licensePlate("ABC123")
                .description("Test incident")
                .isResolved(false)
                .build();

        expectedResponse = IncidentResponse.builder()
                .id(1L)
                .ticketId(1L)
                .licensePlate("ABC123")
                .description("Test incident")
                .isResolved(false)
                .build();
    }

    @Test
    void execute_ShouldReturnIncident_WhenIncidentExists() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentTypeRepository.findById(any())).thenReturn(Optional.empty());
        when(branchRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = getIncidentByIdUseCase.execute(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC123");

        verify(incidentRepository).findById(1L);
        verify(mapper).toResponseWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void execute_ShouldThrowException_WhenIncidentNotFound() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getIncidentByIdUseCase.execute(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found");

        verify(incidentRepository).findById(1L);
        verify(mapper, never()).toResponseWithDetails(any(), any(), any(), any(), any(), any());
    }
}
