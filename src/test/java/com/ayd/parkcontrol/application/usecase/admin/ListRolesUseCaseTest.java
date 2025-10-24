package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.application.mapper.RoleDtoMapper;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRolesUseCaseTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleDtoMapper roleDtoMapper;

    @InjectMocks
    private ListRolesUseCase listRolesUseCase;

    private List<Role> mockRoles;
    private List<RoleResponse> mockRoleResponses;

    @BeforeEach
    void setUp() {
        Role role1 = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema con acceso total")
                .build();

        Role role2 = Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .description("Operador de sucursal para control de entradas/salidas")
                .build();

        mockRoles = Arrays.asList(role1, role2);

        RoleResponse response1 = RoleResponse.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema con acceso total")
                .build();

        RoleResponse response2 = RoleResponse.builder()
                .id(2)
                .name("Operador Sucursal")
                .description("Operador de sucursal para control de entradas/salidas")
                .build();

        mockRoleResponses = Arrays.asList(response1, response2);
    }

    @Test
    void execute_shouldReturnAllRoles() {
        when(roleRepository.findAll()).thenReturn(mockRoles);
        when(roleDtoMapper.toResponse(mockRoles.get(0))).thenReturn(mockRoleResponses.get(0));
        when(roleDtoMapper.toResponse(mockRoles.get(1))).thenReturn(mockRoleResponses.get(1));

        List<RoleResponse> result = listRolesUseCase.execute();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Administrador");
        assertThat(result.get(1).getName()).isEqualTo("Operador Sucursal");

        verify(roleRepository).findAll();
        verify(roleDtoMapper, times(2)).toResponse(any(Role.class));
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoRolesExist() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        List<RoleResponse> result = listRolesUseCase.execute();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(roleRepository).findAll();
        verify(roleDtoMapper, never()).toResponse(any(Role.class));
    }
}
