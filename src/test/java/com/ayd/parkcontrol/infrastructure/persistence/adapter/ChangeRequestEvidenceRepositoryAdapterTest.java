package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestEvidenceEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.ChangeRequestEvidenceMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestEvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeRequestEvidenceRepositoryAdapterTest {

    @Mock
    private JpaChangeRequestEvidenceRepository jpaRepository;

    @Mock
    private ChangeRequestEvidenceMapper mapper;

    @InjectMocks
    private ChangeRequestEvidenceRepositoryAdapter adapter;

    private ChangeRequestEvidence evidence;
    private ChangeRequestEvidenceEntity entity;

    @BeforeEach
    void setUp() {
        evidence = new ChangeRequestEvidence();
        evidence.setId(1L);
        evidence.setFileName("evidence.pdf");

        entity = new ChangeRequestEvidenceEntity();
        entity.setId(1L);
        entity.setFileName("evidence.pdf");
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(mapper.toEntity(evidence)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(evidence);

        // Act
        ChangeRequestEvidence result = adapter.save(evidence);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("evidence.pdf");
        verify(mapper).toEntity(evidence);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenEvidenceExists_ShouldReturnMappedEvidence() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(evidence);

        // Act
        Optional<ChangeRequestEvidence> result = adapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(jpaRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenEvidenceDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChangeRequestEvidence> result = adapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findByChangeRequestId_ShouldReturnEvidenceList() {
        // Arrange
        List<ChangeRequestEvidenceEntity> entities = Collections.singletonList(entity);
        when(jpaRepository.findByChangeRequestId(1L)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(evidence);

        // Act
        List<ChangeRequestEvidence> result = adapter.findByChangeRequestId(1L);

        // Assert
        assertThat(result).hasSize(1);
        verify(jpaRepository).findByChangeRequestId(1L);
    }

    @Test
    void countByChangeRequestId_ShouldDelegateToRepository() {
        // Arrange
        when(jpaRepository.countByChangeRequestId(1L)).thenReturn(3L);

        // Act
        long result = adapter.countByChangeRequestId(1L);

        // Assert
        assertThat(result).isEqualTo(3L);
        verify(jpaRepository).countByChangeRequestId(1L);
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        // Arrange & Act
        adapter.deleteById(1L);

        // Assert
        verify(jpaRepository).deleteById(1L);
    }
}
