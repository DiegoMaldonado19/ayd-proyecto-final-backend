package com.ayd.parkcontrol.presentation.controller.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.CreateFleetRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetDiscountsRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
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

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FleetControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private CreateFleetUseCase createFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private GetFleetUseCase getFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private ListFleetsUseCase listFleetsUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private UpdateFleetUseCase updateFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private DeleteFleetUseCase deleteFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private AddVehicleToFleetUseCase addVehicleToFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private RemoveVehicleFromFleetUseCase removeVehicleFromFleetUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private ListFleetVehiclesUseCase listFleetVehiclesUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private GetFleetDiscountsUseCase getFleetDiscountsUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private UpdateFleetDiscountsUseCase updateFleetDiscountsUseCase;

        @SuppressWarnings("removal")
        @org.springframework.boot.test.mock.mockito.MockBean
        private GetFleetConsumptionUseCase getFleetConsumptionUseCase;

        private FleetResponse fleetResponse;
        private CreateFleetRequest createFleetRequest;
        private UpdateFleetRequest updateFleetRequest;

        @BeforeEach
        void setUp() {
                createFleetRequest = CreateFleetRequest.builder()
                                .name("Test Fleet Company")
                                .taxId("12345678-9")
                                .contactName("John Doe")
                                .corporateEmail("contact@testfleet.com")
                                .phone("50212345678")
                                .corporateDiscountPercentage(new BigDecimal("5.00"))
                                .plateLimit(20)
                                .billingPeriod("MONTHLY")
                                .build();

                fleetResponse = FleetResponse.builder()
                                .id(1L)
                                .name(createFleetRequest.getName())
                                .taxId(createFleetRequest.getTaxId())
                                .contactName(createFleetRequest.getContactName())
                                .corporateEmail(createFleetRequest.getCorporateEmail())
                                .phone(createFleetRequest.getPhone())
                                .corporateDiscountPercentage(createFleetRequest.getCorporateDiscountPercentage())
                                .plateLimit(createFleetRequest.getPlateLimit())
                                .billingPeriod(createFleetRequest.getBillingPeriod())
                                .monthsUnpaid(0)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .activeVehiclesCount(0L)
                                .build();

                updateFleetRequest = UpdateFleetRequest.builder()
                                .name("Updated Fleet Company")
                                .corporateDiscountPercentage(new BigDecimal("7.00"))
                                .build();
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldCreateFleetSuccessfully() throws Exception {
                when(createFleetUseCase.execute(any(CreateFleetRequest.class))).thenReturn(fleetResponse);

                mockMvc.perform(post("/fleets")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createFleetRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value(createFleetRequest.getName()))
                                .andExpect(jsonPath("$.tax_id").value(createFleetRequest.getTaxId()))
                                .andExpect(jsonPath("$.is_active").value(true))
                                .andExpect(jsonPath("$.active_vehicles_count").value(0));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldGetFleetByIdSuccessfully() throws Exception {
                when(getFleetUseCase.execute(1L)).thenReturn(fleetResponse);

                mockMvc.perform(get("/fleets/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value(fleetResponse.getName()));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldListFleetsSuccessfully() throws Exception {
                Page<FleetResponse> page = new PageImpl<>(Collections.singletonList(fleetResponse));
                when(listFleetsUseCase.execute(any(PageRequest.class))).thenReturn(page);

                mockMvc.perform(get("/fleets")
                                .with(csrf())
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1))
                                .andExpect(jsonPath("$.content[0].name").value(fleetResponse.getName()));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldUpdateFleetSuccessfully() throws Exception {
                FleetResponse updatedResponse = FleetResponse.builder()
                                .id(1L)
                                .name("Updated Fleet Company")
                                .taxId(fleetResponse.getTaxId())
                                .corporateDiscountPercentage(new BigDecimal("7.00"))
                                .plateLimit(20)
                                .billingPeriod("MONTHLY")
                                .monthsUnpaid(0)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .activeVehiclesCount(0L)
                                .build();

                when(updateFleetUseCase.execute(eq(1L), any(UpdateFleetRequest.class))).thenReturn(updatedResponse);

                mockMvc.perform(put("/fleets/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateFleetRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Updated Fleet Company"))
                                .andExpect(jsonPath("$.corporate_discount_percentage").value(7.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldDeleteFleetSuccessfully() throws Exception {
                mockMvc.perform(delete("/fleets/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
        void shouldAddVehicleToFleetSuccessfully() throws Exception {
                AddVehicleToFleetRequest request = AddVehicleToFleetRequest.builder()
                                .licensePlate("FLT-001")
                                .planId(1L)
                                .vehicleTypeId(2)
                                .assignedEmployee("John Smith")
                                .build();

                FleetVehicleResponse vehicleResponse = FleetVehicleResponse.builder()
                                .id(1L)
                                .companyId(1L)
                                .licensePlate("FLT-001")
                                .assignedEmployee("John Smith")
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(addVehicleToFleetUseCase.execute(eq(1L), any(AddVehicleToFleetRequest.class)))
                                .thenReturn(vehicleResponse);

                mockMvc.perform(post("/fleets/1/vehicles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.license_plate").value("FLT-001"))
                                .andExpect(jsonPath("$.assigned_employee").value("John Smith"));
        }

        @Test
        @WithMockUser(roles = "Operador Back Office", username = "test@parkcontrol.com")
        void shouldRemoveVehicleFromFleetSuccessfully() throws Exception {
                mockMvc.perform(delete("/fleets/1/vehicles/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldGetFleetDiscountsSuccessfully() throws Exception {
                FleetDiscountsResponse discountsResponse = FleetDiscountsResponse.builder()
                                .corporateDiscountPercentage(new BigDecimal("5.00"))
                                .maxTotalDiscount(new BigDecimal("35.00"))
                                .build();

                when(getFleetDiscountsUseCase.execute(1L)).thenReturn(discountsResponse);

                mockMvc.perform(get("/fleets/1/discounts")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.corporate_discount_percentage").value(5.00))
                                .andExpect(jsonPath("$.max_total_discount").value(35.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldUpdateFleetDiscountsSuccessfully() throws Exception {
                UpdateFleetDiscountsRequest request = UpdateFleetDiscountsRequest.builder()
                                .corporateDiscountPercentage(new BigDecimal("8.00"))
                                .build();

                FleetDiscountsResponse discountsResponse = FleetDiscountsResponse.builder()
                                .corporateDiscountPercentage(new BigDecimal("8.00"))
                                .maxTotalDiscount(new BigDecimal("35.00"))
                                .build();

                when(updateFleetDiscountsUseCase.execute(eq(1L), any(UpdateFleetDiscountsRequest.class)))
                                .thenReturn(discountsResponse);

                mockMvc.perform(put("/fleets/1/discounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.corporate_discount_percentage").value(8.00));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldGetFleetConsumptionSuccessfully() throws Exception {
                com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse consumptionResponse = com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse
                                .builder()
                                .companyId(1L)
                                .companyName("Test Fleet")
                                .totalVehicles(15)
                                .totalEntries(100L)
                                .totalHoursConsumed(new BigDecimal("500.00"))
                                .totalAmountCharged(new BigDecimal("5000.00"))
                                .build();

                when(getFleetConsumptionUseCase.execute(1L)).thenReturn(consumptionResponse);

                mockMvc.perform(get("/fleets/1/consumption")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.company_id").value(1))
                                .andExpect(jsonPath("$.total_vehicles").value(15))
                                .andExpect(jsonPath("$.total_entries").value(100))
                                .andExpect(jsonPath("$.total_amount_charged").value(5000.00));
        }

        @Test
        @WithMockUser(roles = "Operador Back Office")
        void shouldListFleetVehiclesSuccessfully() throws Exception {
                FleetVehicleResponse vehicleResponse = FleetVehicleResponse.builder()
                                .id(1L)
                                .companyId(1L)
                                .licensePlate("ABC123")
                                .isActive(true)
                                .build();

                Page<FleetVehicleResponse> page = new PageImpl<>(Collections.singletonList(vehicleResponse));
                when(listFleetVehiclesUseCase.execute(eq(1L), any(PageRequest.class))).thenReturn(page);

                mockMvc.perform(get("/fleets/1/vehicles")
                                .with(csrf())
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1))
                                .andExpect(jsonPath("$.content[0].license_plate").value("ABC123"));
        }

        @Test
        @WithMockUser(roles = "Operador Back Office")
        void shouldListFleetVehiclesByActiveStatus() throws Exception {
                FleetVehicleResponse vehicleResponse = FleetVehicleResponse.builder()
                                .id(1L)
                                .companyId(1L)
                                .licensePlate("ABC123")
                                .isActive(true)
                                .build();

                Page<FleetVehicleResponse> page = new PageImpl<>(Collections.singletonList(vehicleResponse));
                when(listFleetVehiclesUseCase.executeByActive(eq(1L), eq(true), any(PageRequest.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/fleets/1/vehicles")
                                .with(csrf())
                                .param("isActive", "true")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].is_active").value(true));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void shouldListFleetsByActiveStatus() throws Exception {
                Page<FleetResponse> page = new PageImpl<>(Collections.singletonList(fleetResponse));
                when(listFleetsUseCase.executeByActive(eq(true), any(PageRequest.class))).thenReturn(page);

                mockMvc.perform(get("/fleets")
                                .with(csrf())
                                .param("isActive", "true")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].is_active").value(true));
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void shouldReturnForbiddenForClientRole() throws Exception {
                mockMvc.perform(get("/fleets")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
                mockMvc.perform(get("/fleets"))
                                .andExpect(status().isUnauthorized());
        }
}
