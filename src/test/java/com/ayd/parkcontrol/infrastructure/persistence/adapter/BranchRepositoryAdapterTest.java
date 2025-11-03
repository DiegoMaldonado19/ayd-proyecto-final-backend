package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.BranchMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchRepositoryAdapterTest {

    @Mock
    private JpaBranchRepository jpaBranchRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchRepositoryAdapter branchRepositoryAdapter;

    private Branch branch;
    private BranchEntity branchEntity;

    @BeforeEach
    void setUp() {
        branch = new Branch();
        branch.setId(1L);
        branch.setName("Sucursal Centro");
        branch.setIsActive(true);

        branchEntity = new BranchEntity();
        branchEntity.setId(1L);
        branchEntity.setName("Sucursal Centro");
        branchEntity.setIsActive(true);
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(branchMapper.toEntity(branch)).thenReturn(branchEntity);
        when(jpaBranchRepository.save(branchEntity)).thenReturn(branchEntity);
        when(branchMapper.toDomain(branchEntity)).thenReturn(branch);

        // Act
        Branch result = branchRepositoryAdapter.save(branch);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sucursal Centro");
        verify(branchMapper).toEntity(branch);
        verify(jpaBranchRepository).save(branchEntity);
        verify(branchMapper).toDomain(branchEntity);
    }

    @Test
    void findById_WhenBranchExists_ShouldReturnMappedBranch() {
        // Arrange
        when(jpaBranchRepository.findById(1L)).thenReturn(Optional.of(branchEntity));
        when(branchMapper.toDomain(branchEntity)).thenReturn(branch);

        // Act
        Optional<Branch> result = branchRepositoryAdapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(jpaBranchRepository).findById(1L);
        verify(branchMapper).toDomain(branchEntity);
    }

    @Test
    void findById_WhenBranchDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaBranchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Branch> result = branchRepositoryAdapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaBranchRepository).findById(999L);
        verify(branchMapper, never()).toDomain(any());
    }

    @Test
    void findByName_WhenBranchExists_ShouldReturnMappedBranch() {
        // Arrange
        when(jpaBranchRepository.findByName("Sucursal Centro")).thenReturn(Optional.of(branchEntity));
        when(branchMapper.toDomain(branchEntity)).thenReturn(branch);

        // Act
        Optional<Branch> result = branchRepositoryAdapter.findByName("Sucursal Centro");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sucursal Centro");
        verify(jpaBranchRepository).findByName("Sucursal Centro");
    }

    @Test
    void findAll_ShouldReturnPageOfMappedBranches() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BranchEntity> entityPage = new PageImpl<>(Collections.singletonList(branchEntity));
        when(jpaBranchRepository.findAll(pageable)).thenReturn(entityPage);
        when(branchMapper.toDomain(branchEntity)).thenReturn(branch);

        // Act
        Page<Branch> result = branchRepositoryAdapter.findAll(pageable);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(jpaBranchRepository).findAll(pageable);
    }

    @Test
    void findByIsActive_ShouldReturnActiveBranches() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BranchEntity> entityPage = new PageImpl<>(Collections.singletonList(branchEntity));
        when(jpaBranchRepository.findByIsActive(true, pageable)).thenReturn(entityPage);
        when(branchMapper.toDomain(branchEntity)).thenReturn(branch);

        // Act
        Page<Branch> result = branchRepositoryAdapter.findByIsActive(true, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
        verify(jpaBranchRepository).findByIsActive(true, pageable);
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        // Arrange & Act
        branchRepositoryAdapter.deleteById(1L);

        // Assert
        verify(jpaBranchRepository).deleteById(1L);
    }

    @Test
    void existsByName_ShouldDelegateToRepository() {
        // Arrange
        when(jpaBranchRepository.existsByName("Sucursal Centro")).thenReturn(true);

        // Act
        boolean result = branchRepositoryAdapter.existsByName("Sucursal Centro");

        // Assert
        assertThat(result).isTrue();
        verify(jpaBranchRepository).existsByName("Sucursal Centro");
    }

    @Test
    void existsByNameAndIdNot_ShouldDelegateToRepository() {
        // Arrange
        when(jpaBranchRepository.existsByNameAndIdNot("Sucursal Centro", 2L)).thenReturn(false);

        // Act
        boolean result = branchRepositoryAdapter.existsByNameAndIdNot("Sucursal Centro", 2L);

        // Assert
        assertThat(result).isFalse();
        verify(jpaBranchRepository).existsByNameAndIdNot("Sucursal Centro", 2L);
    }
}
