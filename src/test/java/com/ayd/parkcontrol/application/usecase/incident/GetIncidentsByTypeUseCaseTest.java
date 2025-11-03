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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetIncidentsByTypeUseCaseTest {

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
    private GetIncidentsByTypeUseCase getIncidentsByTypeUseCase;

    private IncidentEntity incident1;
    private IncidentEntity incident2;
    private IncidentTypeEntity incidentType;
    private BranchEntity branch;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        incident1 = IncidentEntity.builder()
                .id(1L)
                .ticketId(100L)
                .incidentTypeId(1)
                .branchId(1L)
                .licensePlate("ABC123")
                .description("Lost ticket case 1")
                .reportedByUserId(1L)
                .isResolved(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        incident2 = IncidentEntity.builder()
                .id(2L)
                .ticketId(101L)
                .incidentTypeId(1)
                .branchId(2L)
                .licensePlate("XYZ789")
                .description("Lost ticket case 2")
                .reportedByUserId(1L)
                .isResolved(true)
                .resolvedByUserId(2L)
                .resolvedAt(LocalDateTime.now())
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

        user = UserEntity.builder()
                .id(1L)
                .email("operator@parkcontrol.com")
                .build();
    }

    @Test
    void execute_WithExistingIncidents_ShouldReturnIncidentsList() {
        // Arrange
        List<IncidentEntity> incidents = Arrays.asList(incident1, incident2);
        IncidentResponse response1 = IncidentResponse.builder().id(1L).incidentTypeId(1).build();
        IncidentResponse response2 = IncidentResponse.builder().id(2L).incidentTypeId(1).build();

        when(incidentRepository.findByIncidentTypeId(1)).thenReturn(incidents);
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(evidenceRepository.countByIncidentId(anyLong())).thenReturn(1L);
        when(mapper.toResponseWithDetails(eq(incident1), any(), any(), any(), any(), anyLong()))
                .thenReturn(response1);
        when(mapper.toResponseWithDetails(eq(incident2), any(), any(), any(), any(), anyLong()))
                .thenReturn(response2);

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(1);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(0).getIncidentTypeId()).isEqualTo(1);
        assertThat(result.get(1).getIncidentTypeId()).isEqualTo(1);
        
        verify(incidentRepository).findByIncidentTypeId(1);
        verify(mapper, times(2)).toResponseWithDetails(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    void execute_WithNoIncidents_ShouldReturnEmptyList() {
        // Arrange
        when(incidentRepository.findByIncidentTypeId(999)).thenReturn(Collections.emptyList());

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(999);

        // Assert
        assertThat(result).isEmpty();
        verify(incidentRepository).findByIncidentTypeId(999);
        verify(mapper, never()).toResponseWithDetails(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    void execute_WithMissingRelatedEntities_ShouldHandleGracefully() {
        // Arrange
        when(incidentRepository.findByIncidentTypeId(1)).thenReturn(Collections.singletonList(incident1));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(IncidentResponse.builder().id(1L).build());

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(1);

        // Assert
        assertThat(result).hasSize(1);
        verify(mapper).toResponseWithDetails(any(), isNull(), isNull(), isNull(), isNull(), eq(0L));
    }

    @Test
    void execute_WithResolvedIncident_ShouldLoadResolvedByUser() {
        // Arrange
        UserEntity resolver = UserEntity.builder()
                .id(2L)
                .email("resolver@parkcontrol.com")
                .build();

        when(incidentRepository.findByIncidentTypeId(1)).thenReturn(Collections.singletonList(incident2));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(resolver));
        when(evidenceRepository.countByIncidentId(2L)).thenReturn(2L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(IncidentResponse.builder().id(2L).build());

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(1);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L); // reportedBy
        verify(userRepository).findById(2L); // resolvedBy
    }

    @Test
    void execute_WithSingleIncidentType_ShouldOnlyReturnThatType() {
        // Arrange
        when(incidentRepository.findByIncidentTypeId(1)).thenReturn(Collections.singletonList(incident1));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(IncidentResponse.builder().id(1L).incidentTypeId(1).build());

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIncidentTypeId()).isEqualTo(1);
        verify(incidentRepository).findByIncidentTypeId(1);
        verify(incidentRepository, never()).findByIncidentTypeId(2);
    }

    @Test
    void execute_WithMultipleEvidences_ShouldReturnCorrectCount() {
        // Arrange
        when(incidentRepository.findByIncidentTypeId(1)).thenReturn(Collections.singletonList(incident1));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(evidenceRepository.countByIncidentId(1L)).thenReturn(5L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), eq(5L)))
                .thenReturn(IncidentResponse.builder().id(1L).evidenceCount(5L).build());

        // Act
        List<IncidentResponse> result = getIncidentsByTypeUseCase.execute(1);

        // Assert
        assertThat(result).hasSize(1);
        verify(evidenceRepository).countByIncidentId(1L);
        verify(mapper).toResponseWithDetails(any(), any(), any(), any(), any(), eq(5L));
    }
}
