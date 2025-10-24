package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    private User mockUser;
    private Role mockRole;
    private UserResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("Test")
                .lastName("User")
                .roleTypeId(2)
                .isActive(true)
                .build();

        mockRole = Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        mockResponse = UserResponse.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .first_name("Test")
                .last_name("User")
                .role_type_id(2)
                .role_name("Operador Sucursal")
                .build();
    }

    @Test
    void execute_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(mockRole));
        when(userDtoMapper.toResponse(any(User.class), any(Role.class))).thenReturn(mockResponse);

        UserResponse result = getUserUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(result.getRole_name()).isEqualTo("Operador Sucursal");

        verify(userRepository).findById(1L);
        verify(roleRepository).findById(2);
        verify(userDtoMapper).toResponse(mockUser, mockRole);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUserUseCase.execute(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verify(roleRepository, never()).findById(any());
        verify(userDtoMapper, never()).toResponse(any(), any());
    }

    @Test
    void execute_shouldHandleNullRole_whenRoleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(2)).thenReturn(Optional.empty());
        when(userDtoMapper.toResponse(any(User.class), isNull())).thenReturn(mockResponse);

        UserResponse result = getUserUseCase.execute(1L);

        assertThat(result).isNotNull();
        verify(userDtoMapper).toResponse(mockUser, null);
    }
}
