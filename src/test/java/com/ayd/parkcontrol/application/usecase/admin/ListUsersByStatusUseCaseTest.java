package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersByStatusUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private ListUsersByStatusUseCase listUsersByStatusUseCase;

    @Test
    void execute_shouldReturnPagedActiveUsers_whenActiveUsersExist() {
        // Given
        Boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);

        Role adminRole = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Admin role")
                .createdAt(LocalDateTime.now())
                .build();

        User user1 = User.builder()
                .id(1L)
                .email("active1@parkcontrol.com")
                .roleTypeId(1)
                .isActive(true)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("active2@parkcontrol.com")
                .roleTypeId(1)
                .isActive(true)
                .build();

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserResponse response1 = UserResponse.builder()
                .id(1L)
                .email("active1@parkcontrol.com")
                .role_name("Administrador")
                .is_active(true)
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(2L)
                .email("active2@parkcontrol.com")
                .role_name("Administrador")
                .is_active(true)
                .build();

        when(roleRepository.findAll()).thenReturn(List.of(adminRole));
        when(userRepository.findByIsActive(eq(isActive), any(Pageable.class))).thenReturn(userPage);
        when(userDtoMapper.toResponse(user1, adminRole)).thenReturn(response1);
        when(userDtoMapper.toResponse(user2, adminRole)).thenReturn(response2);

        // When
        PageResponse<UserResponse> result = listUsersByStatusUseCase.execute(isActive, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage_number());
        assertEquals(10, result.getPage_size());
        assertEquals(2, result.getTotal_elements());
        assertTrue(result.getContent().get(0).getIs_active());
        assertTrue(result.getContent().get(1).getIs_active());

        verify(roleRepository, times(1)).findAll();
        verify(userRepository, times(1)).findByIsActive(eq(isActive), any(Pageable.class));
        verify(userDtoMapper, times(2)).toResponse(any(User.class), any(Role.class));
    }

    @Test
    void execute_shouldReturnPagedInactiveUsers_whenInactiveUsersExist() {
        // Given
        Boolean isActive = false;
        Pageable pageable = PageRequest.of(0, 10);

        Role clientRole = Role.builder()
                .id(4)
                .name("Cliente")
                .description("Client role")
                .createdAt(LocalDateTime.now())
                .build();

        User user1 = User.builder()
                .id(3L)
                .email("inactive1@parkcontrol.com")
                .roleTypeId(4)
                .isActive(false)
                .build();

        List<User> users = List.of(user1);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserResponse response1 = UserResponse.builder()
                .id(3L)
                .email("inactive1@parkcontrol.com")
                .role_name("Cliente")
                .is_active(false)
                .build();

        when(roleRepository.findAll()).thenReturn(List.of(clientRole));
        when(userRepository.findByIsActive(eq(isActive), any(Pageable.class))).thenReturn(userPage);
        when(userDtoMapper.toResponse(user1, clientRole)).thenReturn(response1);

        // When
        PageResponse<UserResponse> result = listUsersByStatusUseCase.execute(isActive, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getIs_active());

        verify(roleRepository, times(1)).findAll();
        verify(userRepository, times(1)).findByIsActive(eq(isActive), any(Pageable.class));
        verify(userDtoMapper, times(1)).toResponse(any(User.class), any(Role.class));
    }

    @Test
    void execute_shouldReturnEmptyPage_whenNoUsersMatchStatus() {
        // Given
        Boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(roleRepository.findAll()).thenReturn(List.of());
        when(userRepository.findByIsActive(eq(isActive), any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<UserResponse> result = listUsersByStatusUseCase.execute(isActive, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotal_elements());

        verify(roleRepository, times(1)).findAll();
        verify(userRepository, times(1)).findByIsActive(eq(isActive), any(Pageable.class));
        verify(userDtoMapper, never()).toResponse(any(), any());
    }

    @Test
    void execute_shouldHandleDescendingSortOrder() {
        // Given
        Boolean isActive = true;

        when(roleRepository.findAll()).thenReturn(List.of());
        when(userRepository.findByIsActive(eq(isActive), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // When
        PageResponse<UserResponse> result = listUsersByStatusUseCase.execute(isActive, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByIsActive(eq(isActive), any(Pageable.class));
    }

    @Test
    void execute_shouldHandleAscendingSortOrder() {
        // Given
        Boolean isActive = false;

        when(roleRepository.findAll()).thenReturn(List.of());
        when(userRepository.findByIsActive(eq(isActive), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // When
        PageResponse<UserResponse> result = listUsersByStatusUseCase.execute(isActive, 0, 10, "email", "asc");

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByIsActive(eq(isActive), any(Pageable.class));
    }
}
