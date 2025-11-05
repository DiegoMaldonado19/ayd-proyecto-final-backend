package com.ayd.parkcontrol.presentation.controller.validation;

import com.ayd.parkcontrol.application.dto.request.validation.ApprovePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.request.validation.RejectPlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.usecase.validation.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlateChangeRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePlateChangeRequestUseCase createPlateChangeRequestUseCase;

    @MockitoBean
    private ListPlateChangeRequestsUseCase listPlateChangeRequestsUseCase;

    @MockitoBean
    private GetPlateChangeRequestUseCase getPlateChangeRequestUseCase;

    @MockitoBean
    private ApprovePlateChangeRequestUseCase approvePlateChangeRequestUseCase;

    @MockitoBean
    private RejectPlateChangeRequestUseCase rejectPlateChangeRequestUseCase;

    @MockitoBean
    private UploadPlateChangeEvidenceUseCase uploadPlateChangeEvidenceUseCase;

    @MockitoBean
    private ListPendingPlateChangeRequestsUseCase listPendingPlateChangeRequestsUseCase;

    @MockitoBean
    private ListPlateChangeRequestsByUserUseCase listPlateChangeRequestsByUserUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "ROLE_Operador Back Office", username = "test@parkcontrol.com")
    void listPlateChangeRequests_ShouldReturnRequestsList() throws Exception {
        PlateChangeRequestResponse request = PlateChangeRequestResponse.builder()
                .id(1L)
                .subscription_id(100L)
                .old_license_plate("ABC123")
                .new_license_plate("XYZ789")
                .status_code("PENDIENTE")
                .created_at(LocalDateTime.now())
                .build();

        when(listPlateChangeRequestsUseCase.execute()).thenReturn(Arrays.asList(request));

        mockMvc.perform(get("/plate-changes")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].old_license_plate").value("ABC123"))
                .andExpect(jsonPath("$.data[0].new_license_plate").value("XYZ789"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Operador Back Office", username = "test@parkcontrol.com")
    void getPlateChangeRequest_ShouldReturnRequest() throws Exception {
        PlateChangeRequestResponse response = PlateChangeRequestResponse.builder()
                .id(1L)
                .subscription_id(100L)
                .old_license_plate("ABC123")
                .new_license_plate("XYZ789")
                .status_code("PENDIENTE")
                .build();

        when(getPlateChangeRequestUseCase.execute(1L)).thenReturn(response);

        mockMvc.perform(get("/plate-changes/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.old_license_plate").value("ABC123"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Operador Back Office", username = "test@parkcontrol.com")
    void approvePlateChange_ShouldReturnApprovedRequest() throws Exception {
        // Este test verifica la aprobación de solicitudes de cambio de placa
        // El use case internamente envía una notificación por email al usuario
        ApprovePlateChangeRequest request = ApprovePlateChangeRequest.builder()
                .review_notes("Documentación verificada correctamente")
                .build();

        PlateChangeRequestResponse response = PlateChangeRequestResponse.builder()
                .id(1L)
                .status_code("APROBADA")
                .reviewed_by(2L)
                .reviewer_name("test@parkcontrol.com")
                .reviewed_at(LocalDateTime.now())
                .build();

        when(approvePlateChangeRequestUseCase.execute(anyLong(), any(ApprovePlateChangeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/plate-changes/1/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status_code").value("APROBADA"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Operador Back Office", username = "test@parkcontrol.com")
    void rejectPlateChange_ShouldReturnRejectedRequest() throws Exception {
        // Este test verifica el rechazo de solicitudes de cambio de placa
        // El use case internamente envía una notificación por email al usuario
        RejectPlateChangeRequest request = RejectPlateChangeRequest.builder()
                .review_notes("Documentación incompleta")
                .build();

        PlateChangeRequestResponse response = PlateChangeRequestResponse.builder()
                .id(1L)
                .status_code("RECHAZADA")
                .reviewed_by(2L)
                .reviewer_name("test@parkcontrol.com")
                .reviewed_at(LocalDateTime.now())
                .review_notes("Documentación incompleta")
                .build();

        when(rejectPlateChangeRequestUseCase.execute(anyLong(), any(RejectPlateChangeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/plate-changes/1/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status_code").value("RECHAZADA"))
                .andExpect(jsonPath("$.data.review_notes").value("Documentación incompleta"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Operador Back Office", username = "test@parkcontrol.com")
    void listPendingRequests_ShouldReturnPendingRequests() throws Exception {
        PlateChangeRequestResponse request1 = PlateChangeRequestResponse.builder()
                .id(1L)
                .status_code("PENDIENTE")
                .build();

        PlateChangeRequestResponse request2 = PlateChangeRequestResponse.builder()
                .id(2L)
                .status_code("PENDIENTE")
                .build();

        List<PlateChangeRequestResponse> pendingRequests = Arrays.asList(request1, request2);

        when(listPendingPlateChangeRequestsUseCase.execute()).thenReturn(pendingRequests);

        mockMvc.perform(get("/plate-changes/pending")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status_code").value("PENDIENTE"))
                .andExpect(jsonPath("$.data[1].status_code").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

}
