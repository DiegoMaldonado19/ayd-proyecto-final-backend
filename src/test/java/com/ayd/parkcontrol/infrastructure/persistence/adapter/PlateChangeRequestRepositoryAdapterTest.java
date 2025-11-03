package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.PlateChangeRequestMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeRequestRepository;
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
class PlateChangeRequestRepositoryAdapterTest {

    @Mock
    private JpaPlateChangeRequestRepository jpaRepository;

    @Mock
    private PlateChangeRequestMapper mapper;

    @InjectMocks
    private PlateChangeRequestRepositoryAdapter adapter;

    private PlateChangeRequest request;
    private PlateChangeRequestEntity entity;

    @BeforeEach
    void setUp() {
        request = new PlateChangeRequest();
        request.setId(1L);
        request.setStatusId(1);

        entity = new PlateChangeRequestEntity();
        entity.setId(1L);
        entity.setStatusId(1);
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(mapper.toEntity(request)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(request);

        // Act
        PlateChangeRequest result = adapter.save(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatusId()).isEqualTo(1);
        verify(mapper).toEntity(request);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenRequestExists_ShouldReturnMappedRequest() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(request);

        // Act
        Optional<PlateChangeRequest> result = adapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(jpaRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenRequestDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<PlateChangeRequest> result = adapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findBySubscriptionId_ShouldReturnSubscriptionRequests() {
        // Arrange
        List<PlateChangeRequestEntity> entities = Collections.singletonList(entity);
        when(jpaRepository.findBySubscriptionId(1L)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(request);

        // Act
        List<PlateChangeRequest> result = adapter.findBySubscriptionId(1L);

        // Assert
        assertThat(result).hasSize(1);
        verify(jpaRepository).findBySubscriptionId(1L);
    }

    @Test
    void findByStatusId_ShouldReturnFilteredRequests() {
        // Arrange
        List<PlateChangeRequestEntity> entities = Collections.singletonList(entity);
        when(jpaRepository.findByStatusId(1)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(request);

        // Act
        List<PlateChangeRequest> result = adapter.findByStatusId(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatusId()).isEqualTo(1);
        verify(jpaRepository).findByStatusId(1);
    }

    @Test
    void findAll_ShouldReturnAllRequestsOrderedByCreatedAt() {
        // Arrange
        List<PlateChangeRequestEntity> entities = Collections.singletonList(entity);
        when(jpaRepository.findAllOrderByCreatedAtDesc()).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(request);

        // Act
        List<PlateChangeRequest> result = adapter.findAll();

        // Assert
        assertThat(result).hasSize(1);
        verify(jpaRepository).findAllOrderByCreatedAtDesc();
    }
}
