package com.ayd.parkcontrol.presentation.controller.report;

import com.ayd.parkcontrol.application.dto.request.report.ExportReportRequest;
import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.application.usecase.report.*;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenerateOccupancyReportUseCase generateOccupancyReportUseCase;

    @MockitoBean
    private GenerateBillingReportUseCase generateBillingReportUseCase;

    @MockitoBean
    private GenerateSubscriptionReportUseCase generateSubscriptionReportUseCase;

    @MockitoBean
    private GenerateCommerceBenefitsReportUseCase generateCommerceBenefitsReportUseCase;

    @MockitoBean
    private GenerateCashClosingReportUseCase generateCashClosingReportUseCase;

    @MockitoBean
    private GenerateIncidentsReportUseCase generateIncidentsReportUseCase;

    @MockitoBean
    private GenerateFleetsReportUseCase generateFleetsReportUseCase;

    @MockitoBean
    private ExportReportUseCase exportReportUseCase;

    @Test
    @WithMockUser(roles = "Administrador")
    void getOccupancyReport_shouldReturnReportData() throws Exception {
        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setVehicleType("2R");
        response.setTotalCapacity(50);
        response.setCurrentOccupancy(25);
        response.setPeakOccupancy(45);

        when(generateOccupancyReportUseCase.execute()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/reports/occupancy")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].branch_id").value(1))
                .andExpect(jsonPath("$.data[0].branch_name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.data[0].vehicle_type").value("2R"))
                .andExpect(jsonPath("$.data[0].total_capacity").value(50));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getBillingReport_shouldReturnReportData() throws Exception {
        BillingReportResponse response = new BillingReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTotalTickets(150L);
        response.setAverageTicketValue(new BigDecimal("100.00"));

        when(generateBillingReportUseCase.execute()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/reports/billing")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].branch_id").value(1))
                .andExpect(jsonPath("$.data[0].branch_name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.data[0].total_revenue").value(15000.50))
                .andExpect(jsonPath("$.data[0].total_tickets").value(150));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getSubscriptionsReport_shouldReturnReportData() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("subscription_id", 1L);
        data.put("status", "ACTIVE");
        when(generateSubscriptionReportUseCase.execute()).thenReturn(Arrays.asList(data));

        mockMvc.perform(get("/reports/subscriptions")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].subscription_id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getCommerceBenefitsReport_shouldReturnReportData() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("total_benefits", 100);
        data.put("active_count", 50L);
        when(generateCommerceBenefitsReportUseCase.execute()).thenReturn(Arrays.asList(data));

        mockMvc.perform(get("/reports/commerce-benefits")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].total_benefits").value(100));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getCashClosingReport_shouldReturnReportData() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("period", "2024-01");
        data.put("total_cash", 25000.00);
        when(generateCashClosingReportUseCase.execute()).thenReturn(Arrays.asList(data));

        mockMvc.perform(get("/reports/cash-closing")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].period").value("2024-01"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getIncidentsReport_shouldReturnReportData() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("incident_id", 1L);
        data.put("type", "DAMAGE");
        when(generateIncidentsReportUseCase.execute()).thenReturn(Arrays.asList(data));

        mockMvc.perform(get("/reports/incidents")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].incident_id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getFleetsReport_shouldReturnReportData() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("fleet_company", "Company A");
        data.put("vehicle_count", 50);
        when(generateFleetsReportUseCase.execute()).thenReturn(Arrays.asList(data));

        mockMvc.perform(get("/reports/fleets")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fleet_company").value("Company A"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void exportReport_withPdfFormat_shouldReturnPdfFile() throws Exception {
        ExportReportRequest request = new ExportReportRequest();
        request.setReportType("OCCUPANCY");
        request.setExportFormat("PDF");

        byte[] mockPdfContent = "Mock PDF Content".getBytes();
        when(exportReportUseCase.execute(any(ExportReportRequest.class))).thenReturn(mockPdfContent);

        mockMvc.perform(post("/reports/export")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(mockPdfContent));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void exportReport_withExcelFormat_shouldReturnExcelFile() throws Exception {
        ExportReportRequest request = new ExportReportRequest();
        request.setReportType("BILLING");
        request.setExportFormat("EXCEL");

        byte[] mockExcelContent = "Mock Excel Content".getBytes();
        when(exportReportUseCase.execute(any(ExportReportRequest.class))).thenReturn(mockExcelContent);

        mockMvc.perform(post("/reports/export")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.excel\""))
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.ms-excel")));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void exportReport_withImageFormat_shouldReturnImageFile() throws Exception {
        ExportReportRequest request = new ExportReportRequest();
        request.setReportType("SUBSCRIPTIONS");
        request.setExportFormat("IMAGE");

        byte[] mockImageContent = "Mock Image Content".getBytes();
        when(exportReportUseCase.execute(any(ExportReportRequest.class))).thenReturn(mockImageContent);

        mockMvc.perform(post("/reports/export")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.image\""))
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getOccupancyReport_withoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/reports/occupancy")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void exportReport_withoutAdminRole_shouldReturnForbidden() throws Exception {
        ExportReportRequest request = new ExportReportRequest();
        request.setReportType("OCCUPANCY");
        request.setExportFormat("PDF");

        mockMvc.perform(post("/reports/export")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
