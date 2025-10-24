package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserStatusRequest;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserStatusUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private UpdateUserStatusUseCase updateUserStatusUseCase;

    @Test
    void execute_shouldUpdateUserStatus_whenUserExists() {
        // Given
        Long userId = 1L;
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .is_active(false)
                .build();

        User existingUser = User.builder()
                .id(userId)
                .email("test@parkcontrol.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .roleTypeId(2)
                .isActive(true)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .email("test@parkcontrol.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .roleTypeId(2)
                .isActive(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        // When
        updateUserStatusUseCase.execute(userId, request);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(roleRepository).findById(2);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Given
        Long userId = 999L;
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .is_active(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> updateUserStatusUseCase.execute(userId, request));
        verify(userRepository, never()).save(any(User.class));
    }
}
