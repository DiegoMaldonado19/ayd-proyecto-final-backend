package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    void execute_shouldDeleteUser_whenUserExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(
                com.ayd.parkcontrol.domain.model.user.User.builder()
                        .id(userId)
                        .email("test@parkcontrol.com")
                        .build()));

        // When & Then
        assertDoesNotThrow(() -> deleteUserUseCase.execute(userId));
        verify(userRepository).deleteById(userId);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.execute(userId));
        verify(userRepository, never()).deleteById(userId);
    }
}
