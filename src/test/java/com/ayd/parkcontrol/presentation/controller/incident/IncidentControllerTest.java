package com.ayd.parkcontrol.presentation.controller.incident;

import com.ayd.parkcontrol.application.dto.request.incident.RegisterIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.ResolveIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.UpdateIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.UploadEvidenceRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.usecase.incident.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ListIncidentsUseCase listIncidentsUseCase;

    @MockitoBean
    private RegisterIncidentUseCase registerIncidentUseCase;

    @MockitoBean
    private GetIncidentByIdUseCase getIncidentByIdUseCase;

    @MockitoBean
    private UpdateIncidentUseCase updateIncidentUseCase;

    @MockitoBean
    private UploadEvidenceUseCase uploadEvidenceUseCase;

    @MockitoBean
    private ListEvidencesByIncidentUseCase listEvidencesByIncidentUseCase;

    @MockitoBean
    private ResolveIncidentUseCase resolveIncidentUseCase;

    @MockitoBean
    private GetIncidentsByBranchUseCase getIncidentsByBranchUseCase;

    @MockitoBean
    private GetIncidentsByTypeUseCase getIncidentsByTypeUseCase;

    private IncidentResponse incidentResponse;
    private RegisterIncidentRequest registerRequest;
    private UpdateIncidentRequest updateRequest;
    private ResolveIncidentRequest resolveRequest;
    private UploadEvidenceRequest uploadEvidenceRequest;
    private IncidentEvidenceResponse evidenceResponse;

    @BeforeEach
    void setUp() {
        incidentResponse = IncidentResponse.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .incidentTypeName("Lost Ticket")
                .branchId(1L)
                .branchName("Centro")
                .reportedByUserId(1L)
                .reportedByUserName("John Doe")
                .licensePlate("ABC123")
                .description("Test incident")
                .isResolved(false)
                .evidenceCount(0L)
                .build();

        registerRequest = RegisterIncidentRequest.builder()
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .licensePlate("ABC123")
                .description("Test incident")
                .build();

        updateRequest = UpdateIncidentRequest.builder()
                .description("Updated description")
                .build();

        resolveRequest = ResolveIncidentRequest.builder()
                .resolutionNotes("Incident resolved")
                .build();

        uploadEvidenceRequest = UploadEvidenceRequest.builder()
                .documentTypeId(1)
                .filePath("/path/to/file.pdf")
                .fileName("evidence.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .notes("Evidence notes")
                .build();

        evidenceResponse = IncidentEvidenceResponse.builder()
                .id(1L)
                .incidentId(1L)
                .documentTypeId(1)
                .documentTypeName("Identification")
                .filePath("/path/to/file.pdf")
                .fileName("evidence.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .uploadedByUserId(1L)
                .uploadedByUserName("John Doe")
                .build();
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listIncidents_ShouldReturnIncidents() throws Exception {
        // Arrange
        List<IncidentResponse> incidents = Arrays.asList(incidentResponse);
        when(listIncidentsUseCase.execute()).thenReturn(incidents);

        // Act & Assert
        mockMvc.perform(get("/incidents")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void registerIncident_ShouldCreateIncident() throws Exception {
        // Arrange
        when(registerIncidentUseCase.execute(any(RegisterIncidentRequest.class)))
                .thenReturn(incidentResponse);

        // Act & Assert
        mockMvc.perform(post("/incidents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getIncidentById_ShouldReturnIncident() throws Exception {
        // Arrange
        when(getIncidentByIdUseCase.execute(1L)).thenReturn(incidentResponse);

        // Act & Assert
        mockMvc.perform(get("/incidents/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void updateIncident_ShouldUpdateAndReturnIncident() throws Exception {
        // Arrange
        when(updateIncidentUseCase.execute(eq(1L), any(UpdateIncidentRequest.class)))
                .thenReturn(incidentResponse);

        // Act & Assert
        mockMvc.perform(put("/incidents/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void uploadEvidence_ShouldCreateEvidence() throws Exception {
        // Arrange
        when(uploadEvidenceUseCase.execute(eq(1L), any(UploadEvidenceRequest.class)))
                .thenReturn(evidenceResponse);

        // Act & Assert
        mockMvc.perform(post("/incidents/1/evidence")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadEvidenceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fileName").value("evidence.pdf"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void listEvidences_ShouldReturnEvidences() throws Exception {
        // Arrange
        List<IncidentEvidenceResponse> evidences = Arrays.asList(evidenceResponse);
        when(listEvidencesByIncidentUseCase.execute(1L)).thenReturn(evidences);

        // Act & Assert
        mockMvc.perform(get("/incidents/1/evidence")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fileName").value("evidence.pdf"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void resolveIncident_ShouldResolveAndReturnIncident() throws Exception {
        // Arrange
        incidentResponse.setIsResolved(true);
        when(resolveIncidentUseCase.execute(eq(1L), any(ResolveIncidentRequest.class)))
                .thenReturn(incidentResponse);

        // Act & Assert
        mockMvc.perform(patch("/incidents/1/resolve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resolveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isResolved").value(true));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getIncidentsByBranch_ShouldReturnIncidents() throws Exception {
        // Arrange
        List<IncidentResponse> incidents = Arrays.asList(incidentResponse);
        when(getIncidentsByBranchUseCase.execute(1L)).thenReturn(incidents);

        // Act & Assert
        mockMvc.perform(get("/incidents/by-branch/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].branchId").value(1L));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getIncidentsByType_ShouldReturnIncidents() throws Exception {
        // Arrange
        List<IncidentResponse> incidents = Arrays.asList(incidentResponse);
        when(getIncidentsByTypeUseCase.execute(1)).thenReturn(incidents);

        // Act & Assert
        mockMvc.perform(get("/incidents/by-type/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].incidentTypeId").value(1));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void listIncidents_ShouldReturnForbidden_WhenClientRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/incidents")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerIncident_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/incidents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isUnauthorized());
    }
}
