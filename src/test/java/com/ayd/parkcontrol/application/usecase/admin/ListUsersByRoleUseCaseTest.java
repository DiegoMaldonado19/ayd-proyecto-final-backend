package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersByRoleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private ListUsersByRoleUseCase listUsersByRoleUseCase;

    @Test
    void execute_shouldReturnPagedUsersByRole_whenRoleExists() {
        // Given
        Integer roleId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        Role adminRole = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Admin role")
                .createdAt(LocalDateTime.now())
                .build();

        User user1 = User.builder()
                .id(1L)
                .email("admin1@parkcontrol.com")
                .roleTypeId(1)
                .isActive(true)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("admin2@parkcontrol.com")
                .roleTypeId(1)
                .isActive(true)
                .build();

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserResponse response1 = UserResponse.builder()
                .id(1L)
                .email("admin1@parkcontrol.com")
                .role_name("Administrador")
                .is_active(true)
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(2L)
                .email("admin2@parkcontrol.com")
                .role_name("Administrador")
                .is_active(true)
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(userRepository.findByRoleTypeId(eq(roleId), any(Pageable.class))).thenReturn(userPage);
        when(userDtoMapper.toResponse(user1, adminRole)).thenReturn(response1);
        when(userDtoMapper.toResponse(user2, adminRole)).thenReturn(response2);

        // When
        PageResponse<UserResponse> result = listUsersByRoleUseCase.execute(roleId, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage_number());
        assertEquals(10, result.getPage_size());
        assertEquals(2, result.getTotal_elements());
        assertEquals("admin1@parkcontrol.com", result.getContent().get(0).getEmail());
        assertEquals("admin2@parkcontrol.com", result.getContent().get(1).getEmail());

        verify(roleRepository, times(1)).findById(roleId);
        verify(userRepository, times(1)).findByRoleTypeId(eq(roleId), any(Pageable.class));
        verify(userDtoMapper, times(2)).toResponse(any(User.class), eq(adminRole));
    }

    @Test
    void execute_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
        // Given
        Integer roleId = 999;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RoleNotFoundException.class,
                () -> listUsersByRoleUseCase.execute(roleId, 0, 10, "createdAt", "desc"));

        verify(roleRepository, times(1)).findById(roleId);
        verify(userRepository, never()).findByRoleTypeId(any(), any());
        verify(userDtoMapper, never()).toResponse(any(), any());
    }

    @Test
    void execute_shouldReturnEmptyPage_whenNoUsersWithRole() {
        // Given
        Integer roleId = 2;
        Pageable pageable = PageRequest.of(0, 10);

        Role operatorRole = Role.builder()
                .id(2)
                .name("Operador")
                .description("Operator role")
                .createdAt(LocalDateTime.now())
                .build();

        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(operatorRole));
        when(userRepository.findByRoleTypeId(eq(roleId), any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<UserResponse> result = listUsersByRoleUseCase.execute(roleId, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotal_elements());

        verify(roleRepository, times(1)).findById(roleId);
        verify(userRepository, times(1)).findByRoleTypeId(eq(roleId), any(Pageable.class));
        verify(userDtoMapper, never()).toResponse(any(), any());
    }
}
