package com.ayd.parkcontrol.presentation.controller.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.request.validation.UpdateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

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
class TemporalPermitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private CreateTemporalPermitUseCase createTemporalPermitUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private ListTemporalPermitsUseCase listTemporalPermitsUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private GetTemporalPermitUseCase getTemporalPermitUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private UpdateTemporalPermitUseCase updateTemporalPermitUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private RevokeTemporalPermitUseCase revokeTemporalPermitUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private ListActiveTemporalPermitsUseCase listActiveTemporalPermitsUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private ListTemporalPermitsBySubscriptionUseCase listTemporalPermitsBySubscriptionUseCase;

    @SuppressWarnings("removal")
    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void listTemporalPermits_ShouldReturnPermitsList() throws Exception {
        TemporalPermitResponse permit = TemporalPermitResponse.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .status("Activo")
                .build();

        when(listTemporalPermitsUseCase.execute()).thenReturn(Arrays.asList(permit));

        mockMvc.perform(get("/temporal-permits")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].temporal_plate").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void createTemporalPermit_ShouldReturnCreatedPermit() throws Exception {
        CreateTemporalPermitRequest request = CreateTemporalPermitRequest.builder()
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .maxUses(10)
                .vehicleTypeId(1)
                .build();

        TemporalPermitResponse response = TemporalPermitResponse.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .build();

        when(createTemporalPermitUseCase.execute(any(CreateTemporalPermitRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/temporal-permits")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.temporal_plate").value("ABC123"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void getTemporalPermit_ShouldReturnPermit() throws Exception {
        TemporalPermitResponse response = TemporalPermitResponse.builder()
                .id(1L)
                .temporalPlate("ABC123")
                .build();

        when(getTemporalPermitUseCase.execute(1L)).thenReturn(response);

        mockMvc.perform(get("/temporal-permits/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void updateTemporalPermit_ShouldReturnUpdatedPermit() throws Exception {
        UpdateTemporalPermitRequest request = UpdateTemporalPermitRequest.builder()
                .maxUses(15)
                .build();

        TemporalPermitResponse response = TemporalPermitResponse.builder()
                .id(1L)
                .maxUses(15)
                .build();

        when(updateTemporalPermitUseCase.execute(anyLong(), any(UpdateTemporalPermitRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/temporal-permits/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.max_uses").value(15));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void revokeTemporalPermit_ShouldReturnRevokedPermit() throws Exception {
        TemporalPermitResponse response = TemporalPermitResponse.builder()
                .id(1L)
                .status("Revocado")
                .build();

        when(revokeTemporalPermitUseCase.execute(1L)).thenReturn(response);

        mockMvc.perform(patch("/temporal-permits/1/revoke")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Revocado"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void listActivePermits_ShouldReturnActivePermits() throws Exception {
        TemporalPermitResponse permit = TemporalPermitResponse.builder()
                .id(1L)
                .status("Activo")
                .build();

        when(listActiveTemporalPermitsUseCase.execute()).thenReturn(Arrays.asList(permit));

        mockMvc.perform(get("/temporal-permits/active")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
    void listPermitsBySubscription_ShouldReturnPermitsForSubscription() throws Exception {
        TemporalPermitResponse permit = TemporalPermitResponse.builder()
                .id(1L)
                .subscriptionId(100L)
                .build();

        when(listTemporalPermitsBySubscriptionUseCase.execute(100L))
                .thenReturn(Arrays.asList(permit));

        mockMvc.perform(get("/temporal-permits/by-subscription/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].subscription_id").value(100));
    }
}
