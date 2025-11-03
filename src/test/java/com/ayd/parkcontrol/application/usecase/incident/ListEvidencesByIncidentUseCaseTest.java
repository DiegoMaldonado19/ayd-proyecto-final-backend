package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListEvidencesByIncidentUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

    @Mock
    private JpaIncidentEvidenceRepository evidenceRepository;

    @Mock
    private JpaDocumentTypeRepository documentTypeRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private IncidentMapper mapper;

    @InjectMocks
    private ListEvidencesByIncidentUseCase listEvidencesByIncidentUseCase;

    private IncidentEntity incident;
    private IncidentEvidenceEntity evidence1;
    private IncidentEvidenceEntity evidence2;
    private DocumentTypeEntity documentType;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(100L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .build();

        evidence1 = IncidentEvidenceEntity.builder()
                .id(1L)
                .incidentId(1L)
                .documentTypeId(1)
                .filePath("https://storage.azure.com/evidence1.jpg")
                .fileName("evidence1.jpg")
                .fileSize(1024L)
                .uploadedByUserId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        evidence2 = IncidentEvidenceEntity.builder()
                .id(2L)
                .incidentId(1L)
                .documentTypeId(2)
                .filePath("https://storage.azure.com/evidence2.pdf")
                .fileName("evidence2.pdf")
                .fileSize(2048L)
                .uploadedByUserId(1L)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        documentType = DocumentTypeEntity.builder()
                .id(1)
                .code("PHOTO")
                .name("Photograph")
                .build();

        user = UserEntity.builder()
                .id(1L)
                .email("operator@parkcontrol.com")
                .build();
    }

    @Test
    void execute_WithExistingEvidences_ShouldReturnEvidencesList() {
        // Arrange
        List<IncidentEvidenceEntity> evidences = Arrays.asList(evidence1, evidence2);
        IncidentEvidenceResponse response1 = IncidentEvidenceResponse.builder().id(1L).build();
        IncidentEvidenceResponse response2 = IncidentEvidenceResponse.builder().id(2L).build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(1L)).thenReturn(evidences);
        when(documentTypeRepository.findById(anyInt())).thenReturn(Optional.of(documentType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEvidenceResponseWithDetails(eq(evidence1), any(), any())).thenReturn(response1);
        when(mapper.toEvidenceResponseWithDetails(eq(evidence2), any(), any())).thenReturn(response2);

        // Act
        List<IncidentEvidenceResponse> result = listEvidencesByIncidentUseCase.execute(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        
        verify(incidentRepository).findById(1L);
        verify(evidenceRepository).findByIncidentIdOrderByCreatedAtDesc(1L);
        verify(mapper, times(2)).toEvidenceResponseWithDetails(any(), any(), any());
    }

    @Test
    void execute_WithNonExistentIncident_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listEvidencesByIncidentUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found with ID: 999");

        verify(incidentRepository).findById(999L);
        verify(evidenceRepository, never()).findByIncidentIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void execute_WithNoEvidences_ShouldReturnEmptyList() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        // Act
        List<IncidentEvidenceResponse> result = listEvidencesByIncidentUseCase.execute(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(incidentRepository).findById(1L);
        verify(evidenceRepository).findByIncidentIdOrderByCreatedAtDesc(1L);
        verify(mapper, never()).toEvidenceResponseWithDetails(any(), any(), any());
    }

    @Test
    void execute_WithMissingDocumentType_ShouldHandleGracefully() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.singletonList(evidence1));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEvidenceResponseWithDetails(any(), any(), any()))
                .thenReturn(IncidentEvidenceResponse.builder().id(1L).build());

        // Act
        List<IncidentEvidenceResponse> result = listEvidencesByIncidentUseCase.execute(1L);

        // Assert
        assertThat(result).hasSize(1);
        verify(mapper).toEvidenceResponseWithDetails(any(), isNull(), any());
    }

    @Test
    void execute_WithMissingUploadedByUser_ShouldHandleGracefully() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.singletonList(evidence1));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(mapper.toEvidenceResponseWithDetails(any(), any(), any()))
                .thenReturn(IncidentEvidenceResponse.builder().id(1L).build());

        // Act
        List<IncidentEvidenceResponse> result = listEvidencesByIncidentUseCase.execute(1L);

        // Assert
        assertThat(result).hasSize(1);
        verify(mapper).toEvidenceResponseWithDetails(any(), any(), isNull());
    }

    @Test
    void execute_ShouldReturnEvidencesInDescendingOrder() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(evidence1, evidence2));
        when(documentTypeRepository.findById(anyInt())).thenReturn(Optional.of(documentType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEvidenceResponseWithDetails(any(), any(), any()))
                .thenAnswer(invocation -> {
                    IncidentEvidenceEntity e = invocation.getArgument(0);
                    return IncidentEvidenceResponse.builder().id(e.getId()).build();
                });

        // Act
        List<IncidentEvidenceResponse> result = listEvidencesByIncidentUseCase.execute(1L);

        // Assert
        assertThat(result).hasSize(2);
        verify(evidenceRepository).findByIncidentIdOrderByCreatedAtDesc(1L);
    }
}
