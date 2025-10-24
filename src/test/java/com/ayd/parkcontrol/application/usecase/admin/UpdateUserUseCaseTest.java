package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateEmailException;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
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
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    private User mockUser;
    private Role mockRole;
    private UpdateUserRequest updateRequest;
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

        updateRequest = UpdateUserRequest.builder()
                .first_name("Updated")
                .last_name("Name")
                .phone("50099999999")
                .build();

        mockResponse = UserResponse.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .first_name("Updated")
                .last_name("Name")
                .phone("50099999999")
                .role_type_id(2)
                .role_name("Operador Sucursal")
                .build();
    }

    @Test
    void execute_shouldUpdateUser_whenValidRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleRepository.findById(2)).thenReturn(Optional.of(mockRole));
        when(userDtoMapper.toResponse(any(User.class), any(Role.class))).thenReturn(mockResponse);
        doNothing().when(userDtoMapper).updateDomain(any(User.class), any(UpdateUserRequest.class));

        UserResponse result = updateUserUseCase.execute(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getFirst_name()).isEqualTo("Updated");
        assertThat(result.getLast_name()).isEqualTo("Name");

        verify(userRepository).findById(1L);
        verify(userDtoMapper).updateDomain(mockUser, updateRequest);
        verify(userRepository).save(mockUser);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUserUseCase.execute(1L, updateRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        UpdateUserRequest requestWithEmail = UpdateUserRequest.builder()
                .email("existing@parkcontrol.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmailAndIdNot("existing@parkcontrol.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> updateUserUseCase.execute(1L, requestWithEmail))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository).existsByEmailAndIdNot("existing@parkcontrol.com", 1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
        UpdateUserRequest requestWithRole = UpdateUserRequest.builder()
                .role_type_id(99)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUserUseCase.execute(1L, requestWithRole))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleRepository).findById(99);
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldNotCheckEmailDuplicate_whenEmailUnchanged() {
        UpdateUserRequest requestWithSameEmail = UpdateUserRequest.builder()
                .email("test@parkcontrol.com")
                .first_name("Updated")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleRepository.findById(2)).thenReturn(Optional.of(mockRole));
        when(userDtoMapper.toResponse(any(User.class), any(Role.class))).thenReturn(mockResponse);
        doNothing().when(userDtoMapper).updateDomain(any(User.class), any(UpdateUserRequest.class));

        updateUserUseCase.execute(1L, requestWithSameEmail);

        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
    }
}
