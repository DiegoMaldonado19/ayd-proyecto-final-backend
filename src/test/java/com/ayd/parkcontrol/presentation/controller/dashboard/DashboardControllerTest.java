package com.ayd.parkcontrol.presentation.controller.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.OccupancyDetailResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.RevenueTodayResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.SystemAlertResponse;
import com.ayd.parkcontrol.application.usecase.dashboard.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    @MockitoBean
    private GetOccupancyDetailsUseCase getOccupancyDetailsUseCase;

    @MockitoBean
    private GetRevenueTodayUseCase getRevenueTodayUseCase;

    @MockitoBean
    private GetActiveSubscriptionsCountUseCase getActiveSubscriptionsCountUseCase;

    @MockitoBean
    private GetSystemAlertsUseCase getSystemAlertsUseCase;

    @MockitoBean
    private GetDashboardByBranchUseCase getDashboardByBranchUseCase;

    @Test
    @WithMockUser(roles = "Administrador")
    void getOverview_shouldReturnDashboardMetrics() throws Exception {
        DashboardOverviewResponse response = new DashboardOverviewResponse();
        response.setTotalBranches(5);
        response.setActiveTickets(120L);
        response.setActiveSubscriptions(350L);
        response.setRevenueToday(new BigDecimal("50000.00"));
        response.setTotalVehiclesToday(250L);
        response.setAverageOccupancyPercentage(75.5);
        response.setPendingIncidents(3L);
        response.setPendingPlateChanges(2L);

        when(getDashboardOverviewUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/dashboard/overview")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.total_branches").value(5))
                .andExpect(jsonPath("$.data.active_tickets").value(120))
                .andExpect(jsonPath("$.data.active_subscriptions").value(350))
                .andExpect(jsonPath("$.data.revenue_today").value(50000.00))
                .andExpect(jsonPath("$.data.average_occupancy_percentage").value(75.5));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getOverview_withOperadorSucursal_shouldReturnDashboardMetrics() throws Exception {
        DashboardOverviewResponse response = new DashboardOverviewResponse();
        response.setTotalBranches(5);
        response.setActiveTickets(60L);
        response.setActiveSubscriptions(150L);
        response.setRevenueToday(new BigDecimal("25000.00"));

        when(getDashboardOverviewUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/dashboard/overview")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.revenue_today").value(25000.00));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getOccupancy_shouldReturnOccupancyDetails() throws Exception {
        OccupancyDetailResponse response = new OccupancyDetailResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalCapacity(100);
        response.setCurrentOccupancy(75);
        response.setOccupancyPercentage(75.0);

        when(getOccupancyDetailsUseCase.execute()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/dashboard/occupancy")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].branch_id").value(1))
                .andExpect(jsonPath("$.data[0].branch_name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.data[0].total_capacity").value(100))
                .andExpect(jsonPath("$.data[0].current_occupancy").value(75))
                .andExpect(jsonPath("$.data[0].occupancy_percentage").value(75.0));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getRevenue_shouldReturnRevenueTodayData() throws Exception {
        RevenueTodayResponse response = new RevenueTodayResponse();
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTicketsRevenue(new BigDecimal("10000.00"));
        response.setSubscriptionsRevenue(new BigDecimal("5000.50"));
        response.setTotalTransactions(200L);
        response.setAverageTicketValue(new BigDecimal("75.00"));

        when(getRevenueTodayUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/dashboard/revenue")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.total_revenue").value(15000.50))
                .andExpect(jsonPath("$.data.tickets_revenue").value(10000.00))
                .andExpect(jsonPath("$.data.total_transactions").value(200))
                .andExpect(jsonPath("$.data.average_ticket_value").value(75.00));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getActiveSubscriptions_shouldReturnSubscriptionCount() throws Exception {
        when(getActiveSubscriptionsCountUseCase.execute()).thenReturn(350L);

        mockMvc.perform(get("/dashboard/active-subscriptions")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(350));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getAlerts_shouldReturnSystemAlerts() throws Exception {
        SystemAlertResponse alert = new SystemAlertResponse();
        alert.setAlertType("CAPACITY_WARNING");
        alert.setSeverity("HIGH");
        alert.setMessage("Sucursal Centro alcanzó 90% de capacidad");
        alert.setBranchId(1L);
        alert.setBranchName("Sucursal Centro");

        when(getSystemAlertsUseCase.execute()).thenReturn(Arrays.asList(alert));

        mockMvc.perform(get("/dashboard/alerts")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].alert_type").value("CAPACITY_WARNING"))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.data[0].message").value("Sucursal Centro alcanzó 90% de capacidad"))
                .andExpect(jsonPath("$.data[0].branch_id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getDashboardByBranch_shouldReturnBranchDashboard() throws Exception {
        DashboardOverviewResponse response = new DashboardOverviewResponse();
        response.setTotalBranches(1);
        response.setRevenueToday(new BigDecimal("10000.00"));
        response.setActiveTickets(50L);
        response.setActiveSubscriptions(120L);
        response.setAverageOccupancyPercentage(80.0);

        when(getDashboardByBranchUseCase.execute(anyLong())).thenReturn(response);

        mockMvc.perform(get("/dashboard/by-branch/1")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.revenue_today").value(10000.00))
                .andExpect(jsonPath("$.data.active_tickets").value(50))
                .andExpect(jsonPath("$.data.active_subscriptions").value(120));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void getOverview_withoutOperadorRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/overview")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void getRevenue_withoutOperadorRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/revenue")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isForbidden());
    }
}
