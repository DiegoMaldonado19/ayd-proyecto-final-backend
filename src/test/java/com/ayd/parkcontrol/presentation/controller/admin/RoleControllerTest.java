package com.ayd.parkcontrol.presentation.controller.admin;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.application.usecase.admin.GetRoleUseCase;
import com.ayd.parkcontrol.application.usecase.admin.ListRolesUseCase;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private ListRolesUseCase listRolesUseCase;

    @SuppressWarnings("removal")
    @MockBean
    private GetRoleUseCase getRoleUseCase;

    private List<RoleResponse> mockRoles;
    private RoleResponse mockRoleResponse;

    @BeforeEach
    void setUp() {
        mockRoleResponse = RoleResponse.builder()
                .id(1)
                .name("Administrador")
                .description("Administrator role")
                .created_at(LocalDateTime.now())
                .build();

        RoleResponse mockRole2 = RoleResponse.builder()
                .id(2)
                .name("Empleado")
                .description("Employee role")
                .created_at(LocalDateTime.now())
                .build();

        mockRoles = Arrays.asList(mockRoleResponse, mockRole2);
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listRoles_shouldReturnAllRoles() throws Exception {
        when(listRolesUseCase.execute()).thenReturn(mockRoles);

        mockMvc.perform(get("/roles")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Administrador"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getRole_shouldReturnRole_whenRoleExists() throws Exception {
        when(getRoleUseCase.execute(1)).thenReturn(mockRoleResponse);

        mockMvc.perform(get("/roles/1")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Administrador"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getRole_shouldReturnNotFound_whenRoleDoesNotExist() throws Exception {
        when(getRoleUseCase.execute(999))
                .thenThrow(new RoleNotFoundException(999));

        mockMvc.perform(get("/roles/999")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getRolePermissions_shouldReturnRoleWithPermissions() throws Exception {
        when(getRoleUseCase.execute(1)).thenReturn(mockRoleResponse);

        mockMvc.perform(get("/roles/1/permissions")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void listRoles_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/roles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "Empleado")
    void listRoles_shouldReturnForbidden_whenNotAuthorized() throws Exception {
        mockMvc.perform(get("/roles")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
