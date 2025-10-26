package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListIncidentsUseCaseTest {

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
    private ListIncidentsUseCase listIncidentsUseCase;

    private IncidentEntity incident1;
    private IncidentEntity incident2;
    private IncidentResponse response1;
    private IncidentResponse response2;

    @BeforeEach
    void setUp() {
        incident1 = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .licensePlate("ABC123")
                .isResolved(false)
                .build();

        incident2 = IncidentEntity.builder()
                .id(2L)
                .ticketId(2L)
                .incidentTypeId(2)
                .branchId(2L)
                .reportedByUserId(2L)
                .licensePlate("XYZ789")
                .isResolved(true)
                .build();

        response1 = IncidentResponse.builder()
                .id(1L)
                .ticketId(1L)
                .licensePlate("ABC123")
                .isResolved(false)
                .build();

        response2 = IncidentResponse.builder()
                .id(2L)
                .ticketId(2L)
                .licensePlate("XYZ789")
                .isResolved(true)
                .build();
    }

    @Test
    void execute_ShouldReturnAllIncidents() {
        // Arrange
        when(incidentRepository.findAll()).thenReturn(Arrays.asList(incident1, incident2));
        when(incidentTypeRepository.findById(any())).thenReturn(java.util.Optional.empty());
        when(branchRepository.findById(any())).thenReturn(java.util.Optional.empty());
        when(userRepository.findById(any())).thenReturn(java.util.Optional.empty());
        when(evidenceRepository.countByIncidentId(any())).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(response1, response2);

        // Act
        List<IncidentResponse> result = listIncidentsUseCase.execute();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(incidentRepository).findAll();
        verify(mapper, times(2)).toResponseWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoIncidents() {
        // Arrange
        when(incidentRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<IncidentResponse> result = listIncidentsUseCase.execute();

        // Assert
        assertThat(result).isEmpty();
        verify(incidentRepository).findAll();
        verify(mapper, never()).toResponseWithDetails(any(), any(), any(), any(), any(), any());
    }
}
