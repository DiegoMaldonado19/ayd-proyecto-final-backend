package com.ayd.parkcontrol.presentation.controller.settlement;

import com.ayd.parkcontrol.application.dto.request.settlement.GenerateSettlementRequest;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementResponse;
import com.ayd.parkcontrol.application.usecase.settlement.GenerateSettlementUseCase;
import com.ayd.parkcontrol.application.usecase.settlement.GetSettlementByIdUseCase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessSettlementHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBusinessSettlementHistoryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenerateSettlementUseCase generateSettlementUseCase;

    @MockitoBean
    private GetSettlementByIdUseCase getSettlementByIdUseCase;

    @MockitoBean
    private JpaBusinessSettlementHistoryRepository settlementRepository;

    @Test
    @WithMockUser(roles = "Administrador")
    void getAllSettlements_shouldReturnAllSettlements() throws Exception {
        BusinessSettlementHistoryEntity settlement = new BusinessSettlementHistoryEntity();
        settlement.setId(1L);
        settlement.setBusinessId(1L);
        settlement.setBranchId(1L);
        settlement.setTotalAmount(new BigDecimal("5000.00"));
        settlement.setTotalHours(new BigDecimal("50.00"));
        settlement.setTicketCount(100);

        when(settlementRepository.findAll()).thenReturn(Arrays.asList(settlement));

        mockMvc.perform(get("/settlements")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].businessId").value(1))
                .andExpect(jsonPath("$[0].branchId").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(5000.00));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getAllSettlements_withoutProperRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/settlements")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void generateSettlement_shouldCreateNewSettlement() throws Exception {
        GenerateSettlementRequest request = new GenerateSettlementRequest();
        request.setBusinessId(1L);
        request.setBranchId(1L);
        request.setPeriodStart(LocalDateTime.now().minusDays(30));
        request.setPeriodEnd(LocalDateTime.now());

        SettlementResponse response = new SettlementResponse();
        response.setId(1L);
        response.setBusinessId(1L);
        response.setBranchId(1L);
        response.setBusinessName("Comercio ABC");
        response.setBranchName("Sucursal Centro");
        response.setTotalAmount(new BigDecimal("5000.00"));
        response.setTotalHours(new BigDecimal("50.00"));
        response.setTicketCount(100);

        when(generateSettlementUseCase.execute(any(GenerateSettlementRequest.class))).thenReturn(response);

        mockMvc.perform(post("/settlements/generate")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.business_id").value(1))
                .andExpect(jsonPath("$.branch_id").value(1))
                .andExpect(jsonPath("$.total_amount").value(5000.00))
                .andExpect(jsonPath("$.ticket_count").value(100));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getSettlementById_shouldReturnSettlement() throws Exception {
        SettlementResponse response = new SettlementResponse();
        response.setId(1L);
        response.setBusinessId(1L);
        response.setBranchId(1L);
        response.setBusinessName("Comercio ABC");
        response.setBranchName("Sucursal Centro");
        response.setTotalAmount(new BigDecimal("5000.00"));
        response.setTotalHours(new BigDecimal("50.00"));

        when(getSettlementByIdUseCase.execute(anyLong())).thenReturn(response);

        mockMvc.perform(get("/settlements/1")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.business_id").value(1))
                .andExpect(jsonPath("$.total_amount").value(5000.00));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getSettlementsByCommerce_shouldReturnCommerceSettlements() throws Exception {
        BusinessSettlementHistoryEntity settlement = new BusinessSettlementHistoryEntity();
        settlement.setId(1L);
        settlement.setBusinessId(1L);
        settlement.setBranchId(1L);
        settlement.setTotalAmount(new BigDecimal("5000.00"));

        when(settlementRepository.findByBusinessIdOrderBySettledAtDesc(anyLong()))
                .thenReturn(Arrays.asList(settlement));

        mockMvc.perform(get("/settlements/by-commerce/1")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].businessId").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getSettlementsByPeriod_shouldReturnSettlementsInDateRange() throws Exception {
        BusinessSettlementHistoryEntity settlement = new BusinessSettlementHistoryEntity();
        settlement.setId(1L);
        settlement.setBusinessId(1L);
        settlement.setTotalAmount(new BigDecimal("5000.00"));

        when(settlementRepository.findByPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(settlement));

        mockMvc.perform(get("/settlements/by-period")
                .header("Authorization", "Bearer mock-jwt-token")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(5000.00));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void getAllSettlements_withoutOperadorRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/settlements")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void generateSettlement_withoutOperadorBackOfficeRole_shouldReturnForbidden() throws Exception {
        GenerateSettlementRequest request = new GenerateSettlementRequest();
        request.setBusinessId(1L);
        request.setBranchId(1L);
        request.setPeriodStart(LocalDateTime.now().minusDays(30));
        request.setPeriodEnd(LocalDateTime.now());

        mockMvc.perform(post("/settlements/generate")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
