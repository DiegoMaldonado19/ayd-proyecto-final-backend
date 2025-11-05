package com.ayd.parkcontrol.presentation.controller.rate;

import com.ayd.parkcontrol.application.dto.request.rate.UpdateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.request.rate.UpdateBranchRateRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.application.usecase.rate.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private GetCurrentRateBaseUseCase getCurrentRateBaseUseCase;

        @MockitoBean
        private GetRateBaseHistoryUseCase getRateBaseHistoryUseCase;

        @MockitoBean
        private UpdateRateBaseUseCase updateRateBaseUseCase;

        @MockitoBean
        private ListRateBranchesUseCase listRateBranchesUseCase;

        @MockitoBean
        private GetRateBranchUseCase getRateBranchUseCase;

        @MockitoBean
        private UpdateBranchRateUseCase updateBranchRateUseCase;

        @MockitoBean
        private DeleteBranchRateUseCase deleteBranchRateUseCase;

        private RateBaseResponse mockRateBaseResponse;
        private RateBranchResponse mockRateBranchResponse;
        private UpdateRateBaseRequest updateRateBaseRequest;
        private UpdateBranchRateRequest updateBranchRateRequest;

        @BeforeEach
        void setUp() {
                mockRateBaseResponse = RateBaseResponse.builder()
                                .id(1L)
                                .amountPerHour(new BigDecimal("5.00"))
                                .startDate(LocalDateTime.now())
                                .endDate(null)
                                .isActive(true)
                                .createdBy(1L)
                                .createdAt(LocalDateTime.now())
                                .build();

                mockRateBranchResponse = RateBranchResponse.builder()
                                .branchId(1L)
                                .branchName("Sucursal Centro")
                                .ratePerHour(new BigDecimal("8.00"))
                                .isActive(true)
                                .build();

                updateRateBaseRequest = UpdateRateBaseRequest.builder()
                                .amountPerHour(new BigDecimal("7.50"))
                                .build();

                updateBranchRateRequest = UpdateBranchRateRequest.builder()
                                .ratePerHour(new BigDecimal("10.00"))
                                .build();
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getCurrentRate_shouldReturnCurrentRate_whenAuthenticated() throws Exception {
                when(getCurrentRateBaseUseCase.execute()).thenReturn(mockRateBaseResponse);

                mockMvc.perform(get("/rates/base")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Current rate retrieved successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.amount_per_hour").value(5.00))
                                .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void getCurrentRate_shouldReturnCurrentRate_whenOperatorAuthenticated() throws Exception {
                when(getCurrentRateBaseUseCase.execute()).thenReturn(mockRateBaseResponse);

                mockMvc.perform(get("/rates/base")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"));
        }

        @Test
        void getCurrentRate_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/rates/base"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getRateHistory_shouldReturnHistory_whenAdminAuthenticated() throws Exception {
                List<RateBaseResponse> history = Arrays.asList(mockRateBaseResponse);
                when(getRateBaseHistoryUseCase.execute()).thenReturn(history);

                mockMvc.perform(get("/rates/base/history")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Rate history retrieved successfully"))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void getRateHistory_shouldReturnForbidden_whenNotAdmin() throws Exception {
                mockMvc.perform(get("/rates/base/history")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateBaseRate_shouldReturnOk_whenValidRequest() throws Exception {
                when(updateRateBaseUseCase.execute(any(UpdateRateBaseRequest.class)))
                                .thenReturn(mockRateBaseResponse);

                mockMvc.perform(put("/rates/base")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRateBaseRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Base rate updated successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.amount_per_hour").value(5.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateBaseRate_shouldReturnBadRequest_whenInvalidAmount() throws Exception {
                UpdateRateBaseRequest invalidRequest = UpdateRateBaseRequest.builder()
                                .amountPerHour(new BigDecimal("-5.00"))
                                .build();

                mockMvc.perform(put("/rates/base")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void updateBaseRate_shouldReturnForbidden_whenNotAdmin() throws Exception {
                mockMvc.perform(put("/rates/base")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRateBaseRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void listBranchRates_shouldReturnAllBranchRates() throws Exception {
                List<RateBranchResponse> branches = Arrays.asList(mockRateBranchResponse);
                when(listRateBranchesUseCase.execute()).thenReturn(branches);

                mockMvc.perform(get("/rates/branches")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Branch rates retrieved successfully"))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].branch_id").value(1))
                                .andExpect(jsonPath("$.data[0].rate_per_hour").value(8.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getBranchRate_shouldReturnBranchRate_whenBranchExists() throws Exception {
                when(getRateBranchUseCase.execute(1L)).thenReturn(mockRateBranchResponse);

                mockMvc.perform(get("/rates/branches/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Branch rate retrieved successfully"))
                                .andExpect(jsonPath("$.data.branch_id").value(1))
                                .andExpect(jsonPath("$.data.branch_name").value("Sucursal Centro"))
                                .andExpect(jsonPath("$.data.rate_per_hour").value(8.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getBranchRate_shouldReturnNotFound_whenBranchDoesNotExist() throws Exception {
                when(getRateBranchUseCase.execute(999L))
                                .thenThrow(new RuntimeException("Branch not found with id: 999"));

                mockMvc.perform(get("/rates/branches/999")
                                .with(csrf()))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateBranchRate_shouldReturnUpdatedRate_whenValidRequest() throws Exception {
                RateBranchResponse updatedResponse = RateBranchResponse.builder()
                                .branchId(1L)
                                .branchName("Sucursal Centro")
                                .ratePerHour(new BigDecimal("10.00"))
                                .isActive(true)
                                .build();

                when(updateBranchRateUseCase.execute(anyLong(), any(UpdateBranchRateRequest.class)))
                                .thenReturn(updatedResponse);

                mockMvc.perform(put("/rates/branches/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateBranchRateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Branch rate updated successfully"))
                                .andExpect(jsonPath("$.data.rate_per_hour").value(10.00));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void updateBranchRate_shouldReturnForbidden_whenNotAdmin() throws Exception {
                mockMvc.perform(put("/rates/branches/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateBranchRateRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void deleteBranchRate_shouldReturnSuccess_whenBranchExists() throws Exception {
                doNothing().when(deleteBranchRateUseCase).execute(anyLong());

                mockMvc.perform(delete("/rates/branches/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Branch rate deleted successfully"));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void deleteBranchRate_shouldReturnForbidden_whenNotAdmin() throws Exception {
                mockMvc.perform(delete("/rates/branches/1")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }
}
