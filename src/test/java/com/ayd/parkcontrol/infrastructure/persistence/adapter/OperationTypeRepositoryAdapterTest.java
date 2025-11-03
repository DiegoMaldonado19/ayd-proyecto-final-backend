package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.infrastructure.persistence.entity.OperationTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.OperationTypeMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaOperationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationTypeRepositoryAdapterTest {

    @Mock
    private JpaOperationTypeRepository jpaRepository;

    @Mock
    private OperationTypeMapper mapper;

    @InjectMocks
    private OperationTypeRepositoryAdapter adapter;

    private OperationType operationType;
    private OperationTypeEntity entity;

    @BeforeEach
    void setUp() {
        operationType = new OperationType();
        operationType.setId(1);
        operationType.setName("INSERT");

        entity = new OperationTypeEntity();
        entity.setId(1);
        entity.setName("INSERT");
    }

    @Test
    void findById_WhenOperationTypeExists_ShouldReturnMappedOperationType() {
        // Arrange
        when(jpaRepository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(operationType);

        // Act
        Optional<OperationType> result = adapter.findById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("INSERT");
        verify(jpaRepository).findById(1);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenOperationTypeDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<OperationType> result = adapter.findById(999);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_ShouldReturnListOfMappedOperationTypes() {
        // Arrange
        List<OperationTypeEntity> entities = Arrays.asList(entity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(operationType);

        // Act
        List<OperationType> result = adapter.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("INSERT");
        verify(jpaRepository).findAll();
    }
}
