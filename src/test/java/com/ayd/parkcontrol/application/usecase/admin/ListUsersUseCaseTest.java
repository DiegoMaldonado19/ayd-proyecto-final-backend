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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private ListUsersUseCase listUsersUseCase;

    @Test
    void execute_shouldReturnPagedUsers_whenUsersExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        Role adminRole = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Admin role")
                .createdAt(LocalDateTime.now())
                .build();

        User user1 = User.builder()
                .id(1L)
                .email("test1@parkcontrol.com")
                .roleTypeId(1)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("test2@parkcontrol.com")
                .roleTypeId(1)
                .build();

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserResponse response1 = UserResponse.builder()
                .id(1L)
                .email("test1@parkcontrol.com")
                .role_name("Administrador")
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(2L)
                .email("test2@parkcontrol.com")
                .role_name("Administrador")
                .build();

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(roleRepository.findAll()).thenReturn(Arrays.asList(adminRole));
        when(userDtoMapper.toResponse(user1, adminRole)).thenReturn(response1);
        when(userDtoMapper.toResponse(user2, adminRole)).thenReturn(response2);

        // When
        PageResponse<UserResponse> result = listUsersUseCase.execute(0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage_number());
        assertEquals(10, result.getPage_size());
        assertEquals(2, result.getTotal_elements());
        assertTrue(result.getIs_first());
        assertTrue(result.getIs_last());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void execute_shouldReturnEmptyPage_whenNoUsersExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<UserResponse> result = listUsersUseCase.execute(0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotal_elements());
        verify(userRepository).findAll(any(Pageable.class));
    }
}
