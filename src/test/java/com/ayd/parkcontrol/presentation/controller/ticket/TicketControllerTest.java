package com.ayd.parkcontrol.presentation.controller.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.ApplyBenefitRequest;
import com.ayd.parkcontrol.application.dto.request.ticket.RegisterEntryRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.BusinessFreeHoursResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketChargeResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.application.usecase.ticket.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterVehicleEntryUseCase registerVehicleEntryUseCase;

    @MockitoBean
    private GetTicketByIdUseCase getTicketByIdUseCase;

    @MockitoBean
    private ProcessVehicleExitUseCase processVehicleExitUseCase;

    @MockitoBean
    private CalculateTicketChargeUseCase calculateTicketChargeUseCase;

    @MockitoBean
    private ApplyCommerceBenefitUseCase applyCommerceBenefitUseCase;

    @MockitoBean
    private GetActiveTicketsUseCase getActiveTicketsUseCase;

    @MockitoBean
    private GetTicketsByBranchUseCase getTicketsByBranchUseCase;

    @MockitoBean
    private GetTicketsByPlateUseCase getTicketsByPlateUseCase;

    @MockitoBean
    private GetTicketByFolioUseCase getTicketByFolioUseCase;

    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        ticketResponse = TicketResponse.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .vehicleTypeName("Cuatro Ruedas")
                .entryTime(LocalDateTime.now())
                .subscriptionId(null)
                .isSubscriber(false)
                .hasIncident(false)
                .statusTypeId(1)
                .statusName("En Curso")
                .qrCode("qr-code")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void registerEntry_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        // Arrange
        RegisterEntryRequest request = RegisterEntryRequest.builder()
                .branchId(1L)
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .build();

        when(registerVehicleEntryUseCase.execute(any(RegisterEntryRequest.class)))
                .thenReturn(ticketResponse);

        // Act & Assert
        mockMvc.perform(post("/tickets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.folio").value("T-1-12345678"))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.isSubscriber").value(false));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void registerEntry_ShouldReturnBadRequest_WhenLicensePlateIsInvalid() throws Exception {
        // Arrange
        RegisterEntryRequest request = RegisterEntryRequest.builder()
                .branchId(1L)
                .licensePlate("INVALID")
                .vehicleTypeId(2)
                .build();

        // Act & Assert
        mockMvc.perform(post("/tickets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getTicket_ShouldReturnOk_WhenTicketExists() throws Exception {
        // Arrange
        when(getTicketByIdUseCase.execute(1L)).thenReturn(ticketResponse);

        // Act & Assert
        mockMvc.perform(get("/tickets/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.folio").value("T-1-12345678"));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void registerExit_ShouldReturnOk_WhenExitIsProcessed() throws Exception {
        // Arrange
        ticketResponse.setExitTime(LocalDateTime.now());
        ticketResponse.setStatusTypeId(2);
        ticketResponse.setStatusName("Finalizado");

        when(processVehicleExitUseCase.execute(1L)).thenReturn(ticketResponse);

        // Act & Assert
        mockMvc.perform(patch("/tickets/1/exit")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exitTime").exists());
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void calculateCharge_ShouldReturnOk_WhenChargeIsCalculated() throws Exception {
        // Arrange
        TicketChargeResponse chargeResponse = TicketChargeResponse.builder()
                .ticketId(1L)
                .totalHours(BigDecimal.valueOf(2.0))
                .freeHoursGranted(BigDecimal.ZERO)
                .billableHours(BigDecimal.valueOf(2.0))
                .rateApplied(BigDecimal.valueOf(10.00))
                .subtotal(BigDecimal.valueOf(20.00))
                .subscriptionHoursConsumed(BigDecimal.ZERO)
                .subscriptionOverageHours(BigDecimal.ZERO)
                .subscriptionOverageCharge(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(20.00))
                .build();

        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);

        // Act & Assert
        mockMvc.perform(get("/tickets/1/calculate-charge")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(20.00));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void applyBenefit_ShouldReturnOk_WhenBenefitIsApplied() throws Exception {
        // Arrange
        ApplyBenefitRequest request = ApplyBenefitRequest.builder()
                .businessId(1L)
                .grantedHours(2.0)
                .build();

        BusinessFreeHoursResponse benefitResponse = BusinessFreeHoursResponse.builder()
                .id(1L)
                .ticketId(1L)
                .businessId(1L)
                .businessName("Business #1")
                .branchId(1L)
                .grantedHours(BigDecimal.valueOf(2.0))
                .grantedAt(LocalDateTime.now())
                .isSettled(false)
                .build();

        when(applyCommerceBenefitUseCase.execute(eq(1L), any(ApplyBenefitRequest.class)))
                .thenReturn(benefitResponse);

        // Act & Assert
        mockMvc.perform(post("/tickets/1/apply-benefit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.grantedHours").value(2.0));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getActiveTickets_ShouldReturnOk_WhenTicketsExist() throws Exception {
        // Arrange
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(getActiveTicketsUseCase.execute()).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/tickets/active")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getTicketsByBranch_ShouldReturnOk_WhenTicketsExist() throws Exception {
        // Arrange
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(getTicketsByBranchUseCase.execute(1L)).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/tickets/by-branch/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].branchId").value(1));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getTicketsByPlate_ShouldReturnOk_WhenTicketsExist() throws Exception {
        // Arrange
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(getTicketsByPlateUseCase.execute("ABC-123")).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/tickets/by-plate/ABC-123")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getTicketByFolio_ShouldReturnOk_WhenTicketExists() throws Exception {
        // Arrange
        when(getTicketByFolioUseCase.execute("T-1-12345678")).thenReturn(ticketResponse);

        // Act & Assert
        mockMvc.perform(get("/tickets/by-folio/T-1-12345678")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folio").value("T-1-12345678"));
    }

    @Test
    void registerEntry_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
        // Arrange
        RegisterEntryRequest request = RegisterEntryRequest.builder()
                .branchId(1L)
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .build();

        // Act & Assert
        mockMvc.perform(post("/tickets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void registerEntry_ShouldReturnForbidden_WhenInsufficientPermissions() throws Exception {
        // Arrange
        RegisterEntryRequest request = RegisterEntryRequest.builder()
                .branchId(1L)
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .build();

        // Act & Assert
        mockMvc.perform(post("/tickets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
