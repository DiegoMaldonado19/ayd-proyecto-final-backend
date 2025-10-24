package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.application.mapper.RoleDtoMapper;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRoleUseCaseTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleDtoMapper roleDtoMapper;

    @InjectMocks
    private GetRoleUseCase getRoleUseCase;

    private Role mockRole;
    private RoleResponse mockRoleResponse;

    @BeforeEach
    void setUp() {
        mockRole = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema con acceso total")
                .build();

        mockRoleResponse = RoleResponse.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema con acceso total")
                .build();
    }

    @Test
    void execute_shouldReturnRole_whenRoleExists() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(mockRole));
        when(roleDtoMapper.toResponse(mockRole)).thenReturn(mockRoleResponse);

        RoleResponse result = getRoleUseCase.execute(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Administrador");

        verify(roleRepository).findById(1);
        verify(roleDtoMapper).toResponse(mockRole);
    }

    @Test
    void execute_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getRoleUseCase.execute(999))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleRepository).findById(999);
        verify(roleDtoMapper, never()).toResponse(any());
    }
}
