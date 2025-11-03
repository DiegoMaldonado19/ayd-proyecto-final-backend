package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.UserMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
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
class UserRepositoryAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private User user;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(userMapper.toEntity(user)).thenReturn(userEntity);
        when(jpaUserRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        User result = userRepositoryAdapter.save(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userMapper).toEntity(user);
        verify(jpaUserRepository).save(userEntity);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnMappedUser() {
        // Arrange
        when(jpaUserRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userRepositoryAdapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(jpaUserRepository).findById(1L);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaUserRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepositoryAdapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaUserRepository).findById(999L);
        verify(userMapper, never()).toDomain(any());
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnMappedUser() {
        // Arrange
        when(jpaUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userRepositoryAdapter.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(jpaUserRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaUserRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepositoryAdapter.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();
        verify(jpaUserRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void findAll_ShouldReturnPageOfMappedUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> entityPage = new PageImpl<>(Collections.singletonList(userEntity));
        when(jpaUserRepository.findAll(pageable)).thenReturn(entityPage);
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Page<User> result = userRepositoryAdapter.findAll(pageable);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(jpaUserRepository).findAll(pageable);
    }

    @Test
    void findByRoleTypeId_ShouldReturnFilteredUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> entityPage = new PageImpl<>(Collections.singletonList(userEntity));
        when(jpaUserRepository.findByRoleTypeId(4, pageable)).thenReturn(entityPage);
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Page<User> result = userRepositoryAdapter.findByRoleTypeId(4, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        verify(jpaUserRepository).findByRoleTypeId(4, pageable);
    }

    @Test
    void findByIsActive_ShouldReturnActiveUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> entityPage = new PageImpl<>(Collections.singletonList(userEntity));
        when(jpaUserRepository.findByIsActive(true, pageable)).thenReturn(entityPage);
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Page<User> result = userRepositoryAdapter.findByIsActive(true, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        verify(jpaUserRepository).findByIsActive(true, pageable);
    }

    @Test
    void existsByEmail_ShouldDelegateToRepository() {
        // Arrange
        when(jpaUserRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userRepositoryAdapter.existsByEmail("test@example.com");

        // Assert
        assertThat(result).isTrue();
        verify(jpaUserRepository).existsByEmail("test@example.com");
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        // Arrange & Act
        userRepositoryAdapter.deleteById(1L);

        // Assert
        verify(jpaUserRepository).deleteById(1L);
    }

    @Test
    void existsByEmailAndIdNot_ShouldDelegateToRepository() {
        // Arrange
        when(jpaUserRepository.existsByEmailAndIdNot("test@example.com", 2L)).thenReturn(false);

        // Act
        boolean result = userRepositoryAdapter.existsByEmailAndIdNot("test@example.com", 2L);

        // Assert
        assertThat(result).isFalse();
        verify(jpaUserRepository).existsByEmailAndIdNot("test@example.com", 2L);
    }
}
