package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RoleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.RoleMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRoleRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private JpaRoleRepository jpaRoleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleRepositoryAdapter roleRepositoryAdapter;

    private Role role;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(4);
        role.setName("Cliente");

        roleEntity = new RoleEntity();
        roleEntity.setId(4);
        roleEntity.setName("Cliente");
    }

    @Test
    void findById_WhenRoleExists_ShouldReturnMappedRole() {
        // Arrange
        when(jpaRoleRepository.findById(4)).thenReturn(Optional.of(roleEntity));
        when(roleMapper.toDomain(roleEntity)).thenReturn(role);

        // Act
        Optional<Role> result = roleRepositoryAdapter.findById(4);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Cliente");
        verify(jpaRoleRepository).findById(4);
        verify(roleMapper).toDomain(roleEntity);
    }

    @Test
    void findById_WhenRoleDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRoleRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleRepositoryAdapter.findById(999);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRoleRepository).findById(999);
        verify(roleMapper, never()).toDomain(any());
    }

    @Test
    void findByName_WhenRoleExists_ShouldReturnMappedRole() {
        // Arrange
        when(jpaRoleRepository.findByName("Cliente")).thenReturn(Optional.of(roleEntity));
        when(roleMapper.toDomain(roleEntity)).thenReturn(role);

        // Act
        Optional<Role> result = roleRepositoryAdapter.findByName("Cliente");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(4);
        verify(jpaRoleRepository).findByName("Cliente");
    }

    @Test
    void findByName_WhenRoleDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRoleRepository.findByName("NonExistentRole")).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleRepositoryAdapter.findByName("NonExistentRole");

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRoleRepository).findByName("NonExistentRole");
    }

    @Test
    void findAll_ShouldReturnListOfMappedRoles() {
        // Arrange
        RoleEntity adminRole = new RoleEntity();
        adminRole.setId(1);
        adminRole.setName("Administrador");

        Role adminRoleDomain = new Role();
        adminRoleDomain.setId(1);
        adminRoleDomain.setName("Administrador");

        List<RoleEntity> entities = Arrays.asList(roleEntity, adminRole);
        when(jpaRoleRepository.findAll()).thenReturn(entities);
        when(roleMapper.toDomain(roleEntity)).thenReturn(role);
        when(roleMapper.toDomain(adminRole)).thenReturn(adminRoleDomain);

        // Act
        List<Role> result = roleRepositoryAdapter.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Cliente");
        assertThat(result.get(1).getName()).isEqualTo("Administrador");
        verify(jpaRoleRepository).findAll();
        verify(roleMapper, times(2)).toDomain(any(RoleEntity.class));
    }

    @Test
    void findAll_WhenNoRoles_ShouldReturnEmptyList() {
        // Arrange
        when(jpaRoleRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Role> result = roleRepositoryAdapter.findAll();

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRoleRepository).findAll();
        verify(roleMapper, never()).toDomain(any());
    }
}
