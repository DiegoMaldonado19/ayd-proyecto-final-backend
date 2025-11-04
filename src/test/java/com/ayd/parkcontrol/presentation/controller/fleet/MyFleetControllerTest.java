package com.ayd.parkcontrol.presentation.controller.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.usecase.fleet.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyFleetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetMyFleetUseCase getMyFleetUseCase;

    @MockitoBean
    private ListMyFleetVehiclesUseCase listMyFleetVehiclesUseCase;

    @MockitoBean
    private AddVehicleToMyFleetUseCase addVehicleToMyFleetUseCase;

    @MockitoBean
    private GetMyFleetConsumptionUseCase getMyFleetConsumptionUseCase;

    private FleetResponse fleetResponse;
    private FleetVehicleResponse fleetVehicleResponse;

    @BeforeEach
    void setUp() {
        fleetResponse = FleetResponse.builder()
                .id(1L)
                .name("My Fleet Company")
                .taxId("12345678-9")
                .contactName("John Doe")
                .corporateEmail("contact@myfleet.com")
                .phone("50212345678")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .activeVehiclesCount(5L)
                .build();

        fleetVehicleResponse = FleetVehicleResponse.builder()
                .id(1L)
                .companyId(1L)
                .licensePlate("FLT-001")
                .assignedEmployee("John Smith")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "Administrador Flotilla")
    void shouldGetMyFleetSuccessfully() throws Exception {
        when(getMyFleetUseCase.execute()).thenReturn(fleetResponse);

        mockMvc.perform(get("/my-fleet")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Fleet Company"))
                .andExpect(jsonPath("$.active_vehicles_count").value(5));
    }

    @Test
    @WithMockUser(roles = "Administrador Flotilla")
    void shouldGetMyFleetVehiclesSuccessfully() throws Exception {
        Page<FleetVehicleResponse> page = new PageImpl<>(Collections.singletonList(fleetVehicleResponse));
        when(listMyFleetVehiclesUseCase.execute(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/my-fleet/vehicles")
                .with(csrf())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].license_plate").value("FLT-001"))
                .andExpect(jsonPath("$.content[0].assigned_employee").value("John Smith"));
    }

    @Test
    @WithMockUser(roles = "Administrador Flotilla")
    void shouldAddVehicleToMyFleetSuccessfully() throws Exception {
        AddVehicleToFleetRequest request = AddVehicleToFleetRequest.builder()
                .licensePlate("FLT-002")
                .planId(1L)
                .vehicleTypeId(2)
                .assignedEmployee("Jane Smith")
                .build();

        FleetVehicleResponse newVehicle = FleetVehicleResponse.builder()
                .id(2L)
                .companyId(1L)
                .licensePlate("FLT-002")
                .assignedEmployee("Jane Smith")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(addVehicleToMyFleetUseCase.execute(any(AddVehicleToFleetRequest.class)))
                .thenReturn(newVehicle);

        mockMvc.perform(post("/my-fleet/vehicles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.license_plate").value("FLT-002"))
                .andExpect(jsonPath("$.assigned_employee").value("Jane Smith"));
    }

    @Test
    @WithMockUser(roles = "Administrador Flotilla")
    void shouldGetMyFleetConsumptionSuccessfully() throws Exception {
        FleetConsumptionResponse consumptionResponse = FleetConsumptionResponse.builder()
                .companyId(1L)
                .companyName("My Fleet Company")
                .totalVehicles(10)
                .totalEntries(150L)
                .totalHoursConsumed(new BigDecimal("750.00"))
                .totalAmountCharged(new BigDecimal("7500.00"))
                .build();

        when(getMyFleetConsumptionUseCase.execute()).thenReturn(consumptionResponse);

        mockMvc.perform(get("/my-fleet/consumption")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company_id").value(1))
                .andExpect(jsonPath("$.company_name").value("My Fleet Company"))
                .andExpect(jsonPath("$.total_vehicles").value(10))
                .andExpect(jsonPath("$.total_entries").value(150))
                .andExpect(jsonPath("$.total_amount_charged").value(7500.00));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void shouldReturnForbiddenForAdminRole() throws Exception {
        mockMvc.perform(get("/my-fleet")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void shouldReturnForbiddenForClientRole() throws Exception {
        mockMvc.perform(get("/my-fleet")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/my-fleet"))
                .andExpect(status().isUnauthorized());
    }
}
