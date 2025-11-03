package com.ayd.parkcontrol.presentation.controller.audit;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.application.usecase.audit.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuditLogController.
 * Valida todos los endpoints de consulta de logs de auditoría.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAuditLogsUseCase listAuditLogsUseCase;

    @MockitoBean
    private GetAuditLogUseCase getAuditLogUseCase;

    @MockitoBean
    private ListAuditLogsByUserUseCase listAuditLogsByUserUseCase;

    @MockitoBean
    private ListAuditLogsByModuleUseCase listAuditLogsByModuleUseCase;

    @MockitoBean
    private ListAuditLogsByDateRangeUseCase listAuditLogsByDateRangeUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "Administrador")
    void listAuditLogs_shouldReturnPageOfLogs() throws Exception {
        AuditLogResponse log = createAuditLogResponse();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(listAuditLogsUseCase.execute(any())).thenReturn(page);

        mockMvc.perform(get("/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].module").value("usuarios"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(listAuditLogsUseCase).execute(any());
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listAuditLogs_withCustomPagination_shouldReturnCustomPage() throws Exception {
        AuditLogResponse log = createAuditLogResponse();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), PageRequest.of(1, 10), 25);

        when(listAuditLogsUseCase.execute(any())).thenReturn(page);

        mockMvc.perform(get("/audit-logs")
                .param("page", "1")
                .param("size", "10")
                .param("sortBy", "module")
                .param("sortDirection", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(25));

        verify(listAuditLogsUseCase).execute(any());
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getAuditLog_shouldReturnLogDetails() throws Exception {
        AuditLogResponse log = createAuditLogResponse();

        when(getAuditLogUseCase.execute(1L)).thenReturn(log);

        mockMvc.perform(get("/audit-logs/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.module").value("usuarios"))
                .andExpect(jsonPath("$.data.entity").value("User"))
                .andExpect(jsonPath("$.data.operation_type").value("CREATE"));

        verify(getAuditLogUseCase).execute(1L);
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listAuditLogsByUser_shouldReturnFilteredLogs() throws Exception {
        AuditLogResponse log = createAuditLogResponse();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(listAuditLogsByUserUseCase.execute(eq(100L), any())).thenReturn(page);

        mockMvc.perform(get("/audit-logs/by-user/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].user_email").value("admin@parkcontrol.com"));

        verify(listAuditLogsByUserUseCase).execute(eq(100L), any());
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listAuditLogsByModule_shouldReturnFilteredLogs() throws Exception {
        AuditLogResponse log = createAuditLogResponse();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(listAuditLogsByModuleUseCase.execute(eq("usuarios"), any())).thenReturn(page);

        mockMvc.perform(get("/audit-logs/by-module/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].module").value("usuarios"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(listAuditLogsByModuleUseCase).execute(eq("usuarios"), any());
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listAuditLogsByDateRange_shouldReturnFilteredLogs() throws Exception {
        AuditLogResponse log = createAuditLogResponse();
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(listAuditLogsByDateRangeUseCase.execute(any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/audit-logs/by-date-range")
                .param("startDate", "2025-01-01T00:00:00")
                .param("endDate", "2025-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(listAuditLogsByDateRangeUseCase).execute(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void listAuditLogs_withoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(listAuditLogsUseCase, never()).execute(any());
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void getAuditLog_withoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/audit-logs/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(getAuditLogUseCase, never()).execute(any());
    }

    private AuditLogResponse createAuditLogResponse() {
        return AuditLogResponse.builder()
                .id(1L)
                .userId(100L)
                .userEmail("admin@parkcontrol.com")
                .module("usuarios")
                .entity("User")
                .operationType("CREATE")
                .description("Usuario creado exitosamente")
                .clientIp("192.168.1.100")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
